package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.element.ClassNode;
import edu.reversing.asm.tree.element.MethodNode;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

public class ExprOrderVisitor extends Visitor {

    @Inject
    public ExprOrderVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true)
                .accept(new ExprVisitor() {
                    @Override
                    public void visitJump(JumpExpr jump) {
                        //TODO abstract JumpExpr, add GotoExpr, IfZeroExpr, and IfCmpExpr
                        //then add visitGoto, visitIfZero, visitIfCmp
                    }

                    @Override
                    public void visitOperation(ArithmeticExpr operation) {
                        Expr left = operation.getLeft();
                        Expr right = operation.getRight();
                    }
                });
    }
}
