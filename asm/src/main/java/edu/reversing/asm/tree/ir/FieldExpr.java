package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

//TODO should this be FieldRefExpr nad FieldStoreExpr?
public class FieldExpr extends Expr {

    public FieldExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public FieldInsnNode getInstruction() {
        return (FieldInsnNode) super.getInstruction();
    }

    public boolean isStatic() {
        int op = getOpcode();
        return op == GETSTATIC || op == PUTSTATIC;
    }

    public boolean isGetting() {
        int op = getOpcode();
        return op == GETSTATIC || op == GETFIELD;
    }

    public boolean isSetting() {
        int op = getOpcode();
        return op == PUTSTATIC || op == PUTFIELD;
    }

    public String key() {
        FieldInsnNode instruction = getInstruction();
        return instruction.owner + "." + instruction.name;
    }

    public String getDescriptor() {
        return getInstruction().desc;
    }
}
