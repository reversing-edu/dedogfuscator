package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.classpath.Library;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.ArithmeticExpr.Operation;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.*;
import edu.reversing.commons.Pair;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.context.DecodeContext;
import edu.reversing.visitor.expr.multiplier.context.EncodeContext;
import org.objectweb.asm.Type;

import java.math.BigInteger;
import java.util.*;

public class MultiplierIdentifier extends Visitor {

  private static final int[] DUPS = {DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2};

  private final Map<String, DecodeContext> decoders = new HashMap<>();
  private final Map<String, DecodeContext> invalidatedDecoders = new HashMap<>();

  private final Map<String, EncodeContext> encoders = new HashMap<>();

  @Inject
  public MultiplierIdentifier(VisitorContext context) {
    super(context);
  }

  private static boolean isDup(int opcode) {
    for (int dup : DUPS) {
      if (opcode == dup) {
        return true;
      }
    }
    return false;
  }

  private static boolean isMultiplicationOperation(ArithmeticExpr expr) {
    if (expr.getOperation() != Operation.MUL) {
      return false;
    }

    Class<? extends Number> type = Operation.getType(expr.getOpcode());
    return type == int.class || type == long.class;
  }

  private static Pair<FieldExpr, NumberExpr> toChildrenPair(ArithmeticExpr expr) {
    Expr lhs = expr.getLeft();
    Expr rhs = expr.getRight();
    if (lhs instanceof FieldExpr field && rhs instanceof NumberExpr num) {
      return new Pair<>(field, num);
    }

    //ExprOrderVisitor should have already switched them so don't need to check rhs for field and lhs for num
    return null;
  }

  @Override
  public void visitCode(ClassNode cls, MethodNode method) {
    ExprTree tree = method.getExprTree(true);
    tree.accept(new ExprVisitor() {
      @Override
      public void visitOperation(ArithmeticExpr expr) {
        extractDecoder(expr);
        extractEncoder(expr);
      }

      @Override
      public void visitAny(Expr expr) {
        if (!isDup(expr.getOpcode())) {
          return;
        }

        extractDupEncoder(expr);
        extractDupDecoder(expr);
      }

      @Override
      public void visitField(FieldExpr field) {
        if (!field.isSetting()) {
          return;
        }

        Type type = field.getType();
        if (!type.getDescriptor().equals("I") && !type.getDescriptor().equals("J")) {
          return;
        }

        extractComplexEncoder(field);
      }
    });
  }

  private void extractComplexEncoder(FieldExpr field) {
    extractEncodersFromAddSub(field);
    extractEncodersFromComplexAddSub(field);
    //extract setting from var
    //and lastly extract setting from self? other fields and methods?
  }

  private void extractEncodersFromAddSub(FieldExpr field) {
    //incremental and decremental operations. i.e field.x += field.x * 29813982
    int addOpcode = Operation.ADD.getOpcode(field.getType());
    int subOpcode = Operation.SUB.getOpcode(field.getType());

    List<Expr> operations = new ArrayList<>();
    operations.addAll(field.getChildren(addOpcode));
    operations.addAll(field.getChildren(subOpcode));

    for (Expr child : operations) {
      ArithmeticExpr operation = (ArithmeticExpr) child;
      ArithmeticExpr layer = (ArithmeticExpr) operation.layer(Operation.MUL.getOpcode(field.getType()));
      if (layer != null) {
        operation = layer;
      }

      if (!(operation.getRight() instanceof NumberExpr number)) {
        return;
      }

      if (operation.getLeft() instanceof FieldExpr other) {
        if (!other.key().equals(field.key())) {
          continue;
        }
      }

      registerEncoder(field, number);
    }
  }

  private void extractEncodersFromComplexAddSub(FieldExpr field) {
    //incremental and decremental operations. i.e jx.h += (jx.s * 2046915735369799591L - var1) * 4093288176164619279L;
    int addOpcode = Operation.ADD.getOpcode(field.getType());
    int subOpcode = Operation.SUB.getOpcode(field.getType());

    List<Expr> operations = new ArrayList<>();
    operations.addAll(field.getChildren(addOpcode));
    operations.addAll(field.getChildren(subOpcode));

    for (Expr child : operations) {
      Expr sub = child.get(1);
      int mulOpcode = Operation.MUL.getOpcode(field.getType());
      if (sub.getOpcode() != mulOpcode) {
        return;
      }

      ArithmeticExpr mul = (ArithmeticExpr) sub;
      if (mul.getLeft().getOpcode() != addOpcode && mul.getLeft().getOpcode() != subOpcode) {
        return;
      }

      if (!(mul.getRight() instanceof NumberExpr number)) {
        return;
      }

      registerEncoder(field, number);
    }
  }

  private void extractDupEncoder(Expr dup) {
    Expr parent = dup.getParent();
    if (!(parent instanceof FieldExpr field) || !field.isSetting()) {
      return;
    }

    Type type = field.getType();
    if (!type.getDescriptor().equals("I") && !type.getDescriptor().equals("J")) {
      return;
    }

    ArithmeticExpr operation = null;
    for (Expr sub : dup) {
      if (sub instanceof ArithmeticExpr) {
        operation = (ArithmeticExpr) sub;
        break;
      }
    }

    if (operation == null) {
      return;
    }

    if (!isMultiplicationOperation(operation)) {
      Expr expr = operation.getLeft();
      if (expr instanceof FieldExpr duped) {
        if (!duped.isGetting() || !duped.key().equals(field.key())) {
          return;
        }

        Expr inner = operation.getRight();
        if (inner instanceof ArithmeticExpr mul && isMultiplicationOperation(mul)) {
          operation = mul;
        }
      }
    }

    Expr rhs = operation.getRight();
    if (!(rhs instanceof NumberExpr constant)) {
      return;
    }

    registerEncoder(field, constant);
  }

  private void extractDupDecoder(Expr dup) {
    Expr parent = dup.getParent();
    if (!(parent instanceof FieldExpr field) || !field.isGetting()) {
      return;
    }

    Type type = field.getType();
    if (!type.getDescriptor().equals("I") && !type.getDescriptor().equals("J")) {
      return;
    }

    Expr root = parent.getParent();
    if (!(root instanceof ArithmeticExpr addsub)) {
      return;
    }

    Expr right = addsub.getRight();
    if (!(right instanceof ArithmeticExpr operation)) {
      return;
    }

    if (!isMultiplicationOperation(operation)) {
      Expr expr = operation.getLeft();
      if (expr instanceof FieldExpr duped) {
        if (!duped.isGetting()) {
          return;
        }

        Expr inner = operation.getRight();
        if (inner instanceof ArithmeticExpr mul && isMultiplicationOperation(mul)) {
          operation = mul;
        }
      }
    }

    Expr rhs = operation.getRight();
    if (!(rhs instanceof NumberExpr constant) || constant.getOpcode() != LDC) {
      return;
    }

    Modulus modulus = new Modulus(BigInteger.valueOf(constant.getValue()), field.getType());
    registerDecoder(field, operation, modulus);
  }

  private void extractDecoder(ArithmeticExpr expr) {
    if (!isMultiplicationOperation(expr)) {
      return;
    }

    Pair<FieldExpr, NumberExpr> children = toChildrenPair(expr);
    if (children == null) {
      return;
    }

    FieldExpr field = children.getLeft();
    NumberExpr constant = children.getRight();
    if (constant.getOpcode() != LDC) {
      return;
    }

    Modulus modulus = new Modulus(BigInteger.valueOf(constant.getValue()), field.getType());
    registerDecoder(field, expr, modulus);
  }

  public void extractEncoder(ArithmeticExpr expr) {
    if (!(expr.getParent() instanceof FieldExpr field)) {
      return;
    }

    Type type = field.getType();
    if (!type.getDescriptor().equals("I") && !type.getDescriptor().equals("J")) {
      return;
    }

    if (!isMultiplicationOperation(expr)) {
      return;
    }

    Expr rhs = expr.getRight();
    if (!(rhs instanceof NumberExpr constant)) {
      return;
    }

        /*Expr lhs = expr.getLeft();
        if (lhs instanceof FieldExpr other) {
            if (!other.key().equals(field.key())) {
                return;
            }
        }*/

    registerEncoder(field, constant);
  }

  public Map<String, DecodeContext> getDecoders() {
    return decoders;
  }

  public Map<String, DecodeContext> getInvalidatedDecoders() {
    return invalidatedDecoders;
  }

  public Map<String, EncodeContext> getEncoders() {
    return encoders;
  }

  public DecodeContext getDecoders(String key) {
    return decoders.get(key);
  }

  public EncodeContext getEncoders(String key) {
    return encoders.get(key);
  }

  public String getTrueKey(FieldExpr field) {
    Library library = context.getLibrary();
    ClassNode owner = library.lookup(field.getOwner());
    if (owner == null) {
      throw new RuntimeException("?");
    }

    if (owner.getField(field.getName(), field.getType().getDescriptor()) != null) {
      return field.key();
    }

    while ((owner = library.lookup(owner.superName)) != null) {
      FieldNode parentField = owner.getField(field.getName(), field.getType().getDescriptor());
      if (parentField != null && (parentField.access & ACC_PRIVATE) == 0) {
        return parentField.key();
      }
    }

    return null;
  }

  private void registerEncoder(FieldExpr field, NumberExpr constant) {
    if (field.getOwner().contains("/") || constant.getOpcode() != LDC) {
      return;
    }

    String key = getTrueKey(field);
    if (key == null) {
      System.err.println("Failed to resolve field key for " + field.key() + " (" + field.getType().getDescriptor() + ") ?");
      return;
    }

    long value = constant.getValue();
    if ((value % 2) == 0) {
      return;
    }

    EncodeContext context = encoders.computeIfAbsent(key, x -> new EncodeContext());
    context.getExpressions().put(field, BigInteger.valueOf(value));
  }

  private void registerDecoder(FieldExpr field, ArithmeticExpr expr, Modulus modulus) {
    if (field.getOwner().contains("/")) {
      return;
    }

    String key = getTrueKey(field);
    if (key == null) {
      System.err.println("Failed to resolve field key for " + field.key() + " (" + field.getType().getDescriptor() + ") ?");
      return;
    }

    Map<String, DecodeContext> map = modulus.validate() ? decoders : invalidatedDecoders;
    DecodeContext context = map.computeIfAbsent(key, x -> new DecodeContext());
    context.getExpressions().put(expr, modulus);
  }

  @Override
  public void output(StringBuilder output) {
    output.append("Identified ");
    output.append(decoders.size()).append(" decoders");
    output.append(" and ");
    output.append(encoders.size()).append(" encoders (potential)");
  }
}
