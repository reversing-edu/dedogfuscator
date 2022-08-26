package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.commons.Pair;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.context.*;
import org.objectweb.asm.Type;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MultiplierIdentifier extends Visitor {

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
        });
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

        //find possible decoders. next up is finding setters to validate contexts of said decoders
        DecodeContext context = decoders.computeIfAbsent(field.key(), x -> new DecodeContext());
        context.getExpressions().put(expr, modulus);
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

        EncodeContext context = encoders.computeIfAbsent(field.key(), x -> new EncodeContext());
        context.getExpressions().put(field, BigInteger.valueOf(constant.getValue()));
    }

    private boolean isMultiplicationOperation(ArithmeticExpr expr) {
        if (expr.getOperation() != ArithmeticExpr.Operation.MUL) {
            return false;
        }

        Class<? extends Number> type = ArithmeticExpr.Operation.getType(expr.getOpcode());
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
}
