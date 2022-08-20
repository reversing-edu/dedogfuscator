package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.commons.Printing;
import edu.reversing.asm.tree.element.ClassNode;
import edu.reversing.asm.tree.element.MethodNode;
import edu.reversing.asm.tree.ir.ArithmeticExpr;
import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.stmt.BinaryJumpExpr;
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
                    public void visitBinaryJump(BinaryJumpExpr stmt) {
                        //process(stmt, stmt.getLeft(), stmt.getRight());
                    }

                    @Override
                    public void visitOperation(ArithmeticExpr operation) {
                        System.out.println(Printing.toString(operation.getInstruction()));
                        //TODO ArithmeticExpr should be split into Unary and Binary too? since INEG for example is unary
                        //process(operation, operation.getLeft(), operation.getRight());
                    }
                });
    }
}
