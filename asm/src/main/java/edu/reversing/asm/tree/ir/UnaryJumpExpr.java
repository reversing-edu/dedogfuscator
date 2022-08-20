package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public class UnaryJumpExpr extends JumpExpr {

    public UnaryJumpExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }
}
