package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;

public class IncrementExpr extends Expr {

  public IncrementExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);
  }

  public IincInsnNode getInstruction() {
    return (IincInsnNode) super.getInstruction();
  }

  public int getChange() {
    return getInstruction().incr;
  }

  public int getVar() {
    return getInstruction().var;
  }
}
