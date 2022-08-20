package edu.reversing.asm.tree.ir;

import edu.reversing.asm.tree.ir.*;
import org.objectweb.asm.tree.AbstractInsnNode;

public final class StoreStmt extends LVTExpr {

    public StoreStmt(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public Expr getValue() {
        return get(0);
    }
}
