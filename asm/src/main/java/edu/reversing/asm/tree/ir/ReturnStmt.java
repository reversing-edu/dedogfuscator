package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ReturnStmt extends Expr {

  public ReturnStmt(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);
  }

  public Expr getValue() {
    return size() > 0 ? get(0) : null;
  }
}
