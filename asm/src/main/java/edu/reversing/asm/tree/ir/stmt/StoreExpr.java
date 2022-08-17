package edu.reversing.asm.tree.ir.stmt;

import edu.reversing.asm.tree.ir.*;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StoreExpr extends LVTExpr {

    public StoreExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public Expr getValue() {
        return get(0);
    }
}
