package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

//TODO merge BasicBlock into this so we don't use labels in the ast
public sealed class JumpExpr extends Expr permits GotoExpr, BinaryJumpStmt, UnaryJumpStmt {

    private TargetExpr target;

    public JumpExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public TargetExpr getTarget() {
        return target;
    }

    public void setTarget(TargetExpr target) {
        if (this.target != null) {
            this.target.getTargeters().remove(this);
        }

        this.target = target;
        if (target == null) {
            getInstruction().label = null;
            return;
        }

        target.getTargeters().add(this);
        getInstruction().label = target.getInstruction();
    }

    public Expr resolve() {
        return target.resolve();
    }

    public JumpInsnNode getInstruction() {
        return (JumpInsnNode) super.getInstruction();
    }
}