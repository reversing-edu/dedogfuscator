package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

//makes it easier to visit expressions
public class DupVisitor extends Visitor {

    private int collapsed = 0;

    @Inject
    public DupVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true).accept(new ExprVisitor() {
            @Override
            public void visitAny(Expr expr) {
                if (expr.getOpcode() != DUP) {
                    return;
                }

                //                System.out.println("DUPPP");
                //              System.out.println(expr);
            }
        });
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Collapsed ").append(collapsed).append(" DUP instructions");
    }
}
