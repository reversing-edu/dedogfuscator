package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

public class InvokeExpr extends Expr {

    public InvokeExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public List<Expr> getArgs() {
        return this;
    }
}
