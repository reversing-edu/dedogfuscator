package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.List;

public class TargetExpr extends Expr {

  private final List<JumpExpr> targeters = new ArrayList<>();

  public TargetExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);
  }

  public List<JumpExpr> getTargeters() {
    return targeters;
  }

  public Expr resolve() {
    Expr expr = this;
    while (expr != null && expr.getOpcode() == -1) {
      expr = expr.getNext();
    }
    return expr != null ? expr : getParent();
  }

  public LabelNode getInstruction() {
    return (LabelNode) super.getInstruction();
  }

  @Override
  public String toString() {
    return getInstruction().toString();
  }
}