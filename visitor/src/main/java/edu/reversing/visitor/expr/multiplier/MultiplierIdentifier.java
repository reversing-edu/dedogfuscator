package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.ArithmeticExpr.Operation;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.commons.Pair;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.context.*;
import org.objectweb.asm.Type;

import java.math.BigInteger;
import java.util.*;

public class MultiplierIdentifier extends Visitor {

    private static final int[] DUPS = {DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2};

    //ew
    private final Map<String, DecodeContext> decoders = new HashMap<>();
    private final Map<String, EncodeContext> encoders = new HashMap<>();

    @Inject
    public MultiplierIdentifier(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        ExprTree tree = method.getExprTree(true);
        tree.accept(new ExprVisitor() {
            @Override
            public void visitOperation(ArithmeticExpr expr) {
                extractDecoder(expr);
                extractEncoder(expr);
                //TODO extract encoders which aren't directly encoded
                //i.e increments, decrements and dup contexts
                //encoders will have the same issue of dups
                //possibly easier to just write a visitor to collapse dups in the bytecode?
            }

            @Override
            public void visitAny(Expr expr) {
                if (!isDup(expr.getOpcode())) {
                    return;
                }

                extractDupEncoder(expr);
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
            Pair<FieldExpr, NumberExpr> pair = toChildrenPair(operation);
            if (pair == null || pair.getLeft().key().equals(field.key())) {
                continue;
            }

            registerEncoder(field, pair.getRight());
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
                if (!(inner instanceof ArithmeticExpr mul) || !isMultiplicationOperation(mul)) {
                    return;
                }

                operation = mul;
                //TODO maybe the field needs to be validated in this case? child of mul would be the GET insn and just compare to above PUT
                //i only ever found this happening once in the client though and this code seems to find it just fine
            }
        }

        Expr rhs = operation.getRight();
        if (!(rhs instanceof NumberExpr constant)) {
            return;
        }

        registerEncoder(field, constant);
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
        NumberExpr encoder = children.getRight();

        Modulus modulus = new Modulus(BigInteger.valueOf(encoder.getValue()), field.getType());
        if (!modulus.validate()) {
            return;
        }

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

        registerEncoder(field, constant);
    }

    private boolean isDup(int opcode) {
        for (int dup : DUPS) {
            if (opcode == dup) {
                return true;
            }
        }
        return false;
    }

    private boolean isMultiplicationOperation(ArithmeticExpr expr) {
        if (expr.getOperation() != Operation.MUL) {
            return false;
        }

        Class<? extends Number> type = Operation.getType(expr.getOpcode());
        return type == int.class || type == long.class;
    }

    private Pair<FieldExpr, NumberExpr> toChildrenPair(ArithmeticExpr expr) {
        Expr lhs = expr.getLeft();
        Expr rhs = expr.getRight();
        if (lhs instanceof FieldExpr field && rhs instanceof NumberExpr num) {
            return new Pair<>(field, num);
        }

        //ExprOrderVisitor should have already switched them so don't need to check rhs for field and lhs for num
        return null;
    }

    public Map<String, DecodeContext> getDecoders() {
        return decoders;
    }

    public Map<String, EncodeContext> getEncoders() {
        return encoders;
    }

    private void registerEncoder(FieldExpr field, NumberExpr constant) {
        EncodeContext context = encoders.computeIfAbsent(field.key(), x -> new EncodeContext());
        context.getExpressions().put(field, BigInteger.valueOf(constant.getValue()));
    }

    private void registerDecoder(FieldExpr field, ArithmeticExpr expr, Modulus modulus) {
        DecodeContext context = decoders.computeIfAbsent(field.key(), x -> new DecodeContext());
        context.getExpressions().put(expr, modulus);
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Identified ");
        output.append(decoders.size()).append(" decoders");
        output.append(" and ");
        output.append(encoders.size()).append(" encoders");
    }
}
