package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.ArithmeticExpr.Operation;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.commons.Multimap;
import edu.reversing.commons.Pair;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

import java.math.BigInteger;

public class MultiplierVisitor extends Visitor {

    private final Multimap<String, Pair<ArithmeticExpr, Modulus>> decoders = new Multimap<>();
    private final Multimap<String, Pair<FieldExpr, BigInteger>> encoders = new Multimap<>();

    @Inject
    public MultiplierVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        ExprTree tree = method.getExprTree(true);

        tree.accept(new ExprVisitor() {
            @Override
            public void visitOperation(ArithmeticExpr expr) {
                if (!isMultiplicationOperation(expr)) {
                    return;
                }

                Pair<FieldExpr, NumberExpr> children = toChildrenPair(expr);
                if (children == null) {
                    return;
                }

                FieldExpr field = children.getLeft();
                NumberExpr encoder = children.getRight();

                Modulus modulus = new Modulus(BigInteger.valueOf(encoder.getValue()), field.getDescriptor());
                if (!modulus.validate()) {
                    return;
                }

                //find possible decoders. next up is finding setters to validate contexts of said decoders
                decoders.put(field.key(), new Pair<>(expr, modulus));
            }

            @Override
            public void visitField(FieldExpr field) {
                if (!field.isSetting()) {
                    return;
                }

                if (!field.getDescriptor().equals("I") && !field.getDescriptor().equals("J")) {
                    return;
                }

                field.accept(new ExprVisitor() {
                    @Override
                    public void visitOperation(ArithmeticExpr expr) {
                        if (!isMultiplicationOperation(expr)) {
                            return;
                        }

                        Expr rhs = expr.getRight();
                        if (!(rhs instanceof NumberExpr constant)) {
                            return;
                        }

                        encoders.put(field.key(), new Pair<>(field, BigInteger.valueOf(constant.getValue())));
                    }
                });
            }
        });
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
}
