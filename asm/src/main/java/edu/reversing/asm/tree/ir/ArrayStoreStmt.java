package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ArrayStoreStmt extends Expr {

  public ArrayStoreStmt(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
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
