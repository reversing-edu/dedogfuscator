package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public class VarExpr extends LVTExpr {

    public VarExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    @Override
    public String toSourceString() {
        return "var" + getIndex();
    }
}
