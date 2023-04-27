package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public abstract sealed class LVTExpr extends Expr permits StoreStmt, VarExpr {

  protected LVTExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);
  }

  public VarInsnNode getInstruction() {
    return (VarInsnNode) super.getInstruction();
  }

  public int getIndex() {
    return getInstruction().var;
  }
}
