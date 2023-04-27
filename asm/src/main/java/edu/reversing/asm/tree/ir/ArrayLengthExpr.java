package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ArrayLengthExpr extends Expr {

  public ArrayLengthExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);
  }

  public Expr getArray() {
    return get(0);
  }
}
