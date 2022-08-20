package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.element.ClassNode;
import edu.reversing.asm.tree.element.MethodNode;
import edu.reversing.asm.tree.ir.*;
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
                        process(stmt, stmt.getLeft(), stmt.getRight());
                    }

                    @Override
                    public void visitOperation(ArithmeticExpr operation) {
                        process(operation, operation.getLeft(), operation.getRight());
                    }

                    @Override
                    public void visitInvoke(InvokeExpr call) {

                    }

                    private void process(Expr parent, Expr lhs, Expr rhs) {
                        System.out.println(parent);
                        System.out.println();

                        //null = any, ? = unsure
                        Class[][] orders = {
                                {null, ArrayLengthExpr.class},
                                {null, NumberExpr.class}, //?
                                {VarExpr.class, FieldExpr.class}, //?
                                {ArrayLoadExpr.class, null}, //?
                                {InvokeExpr.class, null} //?
                        };
     /*
                        List of operators that are safe to reorder:
                        MUL, ADD, IF, more?
                         */
                    }
                });
    }
}
