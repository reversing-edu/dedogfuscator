package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public final class BinaryJumpStmt extends JumpExpr {

  public BinaryJumpStmt(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);
  }

  public Expr getLeft() {
    return get(0);
  }

  public Expr getRight() {
    return get(1);
  }
}
