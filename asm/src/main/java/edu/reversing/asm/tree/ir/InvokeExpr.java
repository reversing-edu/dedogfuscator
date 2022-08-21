package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

public class InvokeExpr extends Expr {

    public InvokeExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public List<Expr> getArgs() {
        return this;
    }

    @Override
    public MethodInsnNode getInstruction() {
        return (MethodInsnNode) super.getInstruction();
    }

    public String key() {
        MethodInsnNode instruction = getInstruction();
        return instruction.owner + "." + instruction.name + instruction.desc;
    }
}
