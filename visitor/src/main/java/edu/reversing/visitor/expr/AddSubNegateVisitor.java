package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.ArithmeticExpr.Operation;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;

public class AddSubNegateVisitor extends Visitor {

    private int flipped = 0;

    @Inject
    public AddSubNegateVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true).accept(new ExprVisitor() {
            @Override
            public void visitOperation(ArithmeticExpr expr) {
                Operation op = expr.getOperation();
                if (op != Operation.ADD && op != Operation.SUB) {
                    return;
                }

                Expr context = expr.getParent();
                if (context instanceof FieldExpr || context instanceof StoreStmt) {
                    return;
                }

                Expr lhs = expr.getLeft();
                Expr rhs = expr.getRight();

                if (lhs instanceof NumberExpr number) {
                    process(expr, number, op);
                } else if (rhs instanceof NumberExpr number) {
                    process(expr, number, op);
                }
            }
        });
    }

    private void process(ArithmeticExpr expr, NumberExpr number, Operation op) {
        if (number.getSign() != NumberExpr.Sign.NEGATIVE || number.getOpcode() == Opcodes.ICONST_M1) {
            return;
        }

        Class<? extends Number> type = Operation.getType(expr.getOpcode());
        Operation newOp = op == Operation.ADD ? Operation.SUB : Operation.ADD;

        expr.setInstruction(new InsnNode(newOp.getOpcode(type)));
        //number.setValue(Math.abs(number.getValue()));

        System.out.println(expr.getRoot().getMethod().key() + " flip " + number.getValue() + expr.getParent());
        flipped++;
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Normalized ").append(flipped).append(" add/sub negation operations");
    }
}
