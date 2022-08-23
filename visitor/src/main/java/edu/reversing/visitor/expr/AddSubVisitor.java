package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.ArithmeticExpr;
import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

public class AddSubVisitor extends Visitor {

    private int flipped = 0;

    @Inject
    public AddSubVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true).accept(new ExprVisitor() {
            @Override
            public void visitOperation(ArithmeticExpr expr) {
                ArithmeticExpr.Operation operation = expr.getOperation();
                if (operation != ArithmeticExpr.Operation.ADD && operation != ArithmeticExpr.Operation.SUB) {
                    return;
                }

                Expr lhs = expr.getLeft();
                Expr rhs = expr.getRight();
            }
        });
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Normalized ").append(flipped).append(" add/sub operations");
    }
}
