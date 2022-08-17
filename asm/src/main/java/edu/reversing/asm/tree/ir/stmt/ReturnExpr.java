package edu.reversing.asm.tree.ir.stmt;

import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.ExprTree;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ReturnExpr extends Expr {

    public ReturnExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public Expr getValue() {
        return size() > 0 ? get(0) : null;
    }
}
