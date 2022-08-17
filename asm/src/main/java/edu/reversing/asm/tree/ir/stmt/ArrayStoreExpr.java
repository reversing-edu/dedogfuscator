package edu.reversing.asm.tree.ir.stmt;

import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.ExprTree;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ArrayStoreExpr extends Expr {

    public ArrayStoreExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public Expr getArray() {
        return get(0);
    }

    public Expr getIndex() {
        return get(1);
    }

    public Expr getValue() {
        return get(2);
    }
}
