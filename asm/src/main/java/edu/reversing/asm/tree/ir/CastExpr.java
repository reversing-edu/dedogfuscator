package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class CastExpr extends Expr {

    public CastExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public TypeInsnNode getInstruction() {
        return (TypeInsnNode) super.getInstruction();
    }

}
