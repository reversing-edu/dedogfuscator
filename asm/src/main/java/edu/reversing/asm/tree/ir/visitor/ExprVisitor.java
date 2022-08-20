package edu.reversing.asm.tree.ir.visitor;

import edu.reversing.asm.tree.ir.*;
import org.objectweb.asm.Opcodes;

public abstract class ExprVisitor implements Opcodes {

    private final ExprVisitor delegate;

    public ExprVisitor(ExprVisitor delegate) {
        this.delegate = delegate;
    }

    public ExprVisitor() {
        this(null);
    }

    public void visitAny(Expr expr) {
        if (delegate != null) {
            delegate.visitAny(expr);
        }
    }

    public void visitIncrement(IncrementExpr increment) {
        if (delegate != null) {
            delegate.visitIncrement(increment);
        }
    }

    public void visitJump(JumpExpr jump) {
        if (delegate != null) {
            delegate.visitJump(jump);
        }
    }

    public void visitBinaryJump(BinaryJumpStmt stmt) {
        if (delegate != null) {
            delegate.visitBinaryJump(stmt);
        }
    }

    public void visitUnaryJump(UnaryJumpStmt stmt) {
        if (delegate != null) {
            delegate.visitUnaryJump(stmt);
        }
    }

    public void visitGoto(GotoExpr jump) {
        if (delegate != null) {
            delegate.visitGoto(jump);
        }
    }

    public void visitNumber(NumberExpr number) {
        if (delegate != null) {
            delegate.visitNumber(number);
        }
    }

    public void visitOperation(ArithmeticExpr operation) {
        if (delegate != null) {
            delegate.visitOperation(operation);
        }
    }

    public void visitVar(VarExpr variable) {
        if (delegate != null) {
            delegate.visitVar(variable);
        }
    }

    public void visitArrayLoad(ArrayLoadExpr load) {
        if (delegate != null) {
            delegate.visitArrayLoad(load);
        }
    }

    public void visitArrayStore(ArrayStoreStmt store) {
        if (delegate != null) {
            delegate.visitArrayStore(store);
        }
    }

    public void visitStore(StoreStmt store) {
        if (delegate != null) {
            delegate.visitStore(store);
        }
    }

    public void visitReturn(ReturnStmt ret) {
        if (delegate != null) {
            delegate.visitReturn(ret);
        }
    }

    public void visitField(FieldExpr field) {
        if (delegate != null) {
            delegate.visitField(field);
        }
    }

    public void visitInvoke(InvokeExpr call) {
        if (delegate != null) {
            delegate.visitInvoke(call);
        }
    }
}