package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.commons.Printing;
import edu.reversing.asm.tree.ClassNode;
import edu.reversing.asm.tree.MethodNode;
import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.ReturnExpr;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

public class PrintExprTest extends Visitor {

    @Inject
    public PrintExprTest(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        System.out.println("Tree: " + cls.name + "." + method.name + method.desc);
        for (Expr expr : method.getExprTree(true)) {
            if (expr instanceof ReturnExpr) {
                System.out.println(Printing.toString(expr.getInstruction()) + " " + expr.size());
            }
        }
    }
}
