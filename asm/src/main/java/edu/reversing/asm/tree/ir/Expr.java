package edu.reversing.asm.tree.ir;

import edu.reversing.asm.commons.Printing;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.commons.Tree;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;

public class Expr extends Tree<Expr> implements Opcodes {

  private final ExprTree tree;

  protected int consume;
  protected int produce;
  protected int remaining;

  protected TryCatchMeta tryCatchMeta;

  private AbstractInsnNode instruction;

  protected Expr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    this.tree = tree;
    this.instruction = instruction;
    this.consume = consume;
    this.produce = remaining = produce;
  }

  public ExprTree getRoot() {
    return tree;
  }

  public int getCumulativeSize() {
    int size = 1;
    for (Expr n : this) {
      size += n.getCumulativeSize();
    }
    return size;
  }

  public Expr[] getProducing() {
    Expr[] exprs = new Expr[size()];
    int i = 0;
    for (Expr child : this) {
      if (child.produce > 0) {
        exprs[i++] = child;
      }
    }
    return exprs;
  }

  public AbstractInsnNode[] collapse() {
    AbstractInsnNode[] instructions = new AbstractInsnNode[getCumulativeSize()];

    int i = 0;
    for (Expr child : this) {
      AbstractInsnNode[] sub = child.collapse();
      System.arraycopy(sub, 0, instructions, i, sub.length);
      i += sub.length;
    }

    if (instructions.length - i != 1) {
      throw new RuntimeException();
    }

    instructions[i] = instruction;
    return instructions;
  }

  public Expr getPrevious() {
    Expr parent = getParent();
    int index = parent.indexOf(this);
    return index >= 0 ? parent.get(index - 1) : null;
  }

  public Expr getNext() {
    Expr parent = getParent();
    int index = parent.indexOf(this);
    return index < parent.size() ? parent.get(index + 1) : null;
  }

  public int getOpcode() {
    return instruction.getOpcode();
  }

  //TODO i dont like having to override this in other exprs
  //generics create too many <?> instances because getNext/getPrevious and other functions dont know types
  public AbstractInsnNode getInstruction() {
    return instruction;
  }

  public void setInstruction(AbstractInsnNode instruction) {
    getRoot().getMethod().instructions.set(this.instruction, instruction);
    this.instruction = instruction;
  }

  //TODO set(expr)
  //TODO visitor

  //should we make some Query api and move layer/preLayer/getChildren out of Expr?
  public List<Expr> getChildren(int opcode) {
    List<Expr> children = new ArrayList<>();
    for (Expr expr : this) {
      if (expr.getOpcode() == opcode) {
        children.add(expr);
      }
    }
    return children;
  }

  public List<Expr> layerAll(int... opcodes) {
    List<Expr> children = getChildren(opcodes[0]);
    if (children.isEmpty() || opcodes.length == 1) {
      return children;
    }

    for (int i = 1; i < opcodes.length; i++) {
      List<Expr> next = new ArrayList<>();
      for (Expr n : children) {
        List<Expr> match = n.getChildren(opcodes[i]);
        if (match != null) {
          next.addAll(match);
        }
      }

      if (next.isEmpty()) {
        return Collections.emptyList();
      }

      children.clear();
      children.addAll(next);
    }

    return children;
  }

  public Expr layer(int... opcodes) {
    List<Expr> nodes = layerAll(opcodes);
    return !nodes.isEmpty() ? nodes.get(0) : null;
  }

  public Expr preLayer(int... opcodes) {
    Expr node = this;
    for (int opcode : opcodes) {
      if ((node = node.getParent()) == null || node.getOpcode() != opcode) {
        return null;
      }
    }
    return node;
  }

  public void accept(ExprVisitor v) {
    v.visitAny(this);
    if (this instanceof JumpExpr) {
      //TODO should this run for all jumps or only for insns besides binary unary and goto (ret jsr etc)
      //or maybe we make JumpExpr abstract and have another impl for jsr/ret
      v.visitJump((JumpExpr) this);
      if (this instanceof BinaryJumpStmt) {
        v.visitBinaryJump((BinaryJumpStmt) this);
      } else if (this instanceof UnaryJumpStmt) {
        v.visitUnaryJump((UnaryJumpStmt) this);
      } else if (this instanceof GotoExpr) {
        v.visitGoto((GotoExpr) this);
      }
    } else if (this instanceof ArithmeticExpr) {
      v.visitOperation((ArithmeticExpr) this);
    } else if (this instanceof ArrayLoadExpr) {
      v.visitArrayLoad((ArrayLoadExpr) this);
    } else if (this instanceof ArrayStoreStmt) {
      v.visitArrayStore((ArrayStoreStmt) this);
    } else if (this instanceof FieldExpr) {
      v.visitField((FieldExpr) this);
    } else if (this instanceof IncrementExpr) {
      v.visitIncrement((IncrementExpr) this);
    } else if (this instanceof NumberExpr) {
      v.visitNumber((NumberExpr) this);
    } else if (this instanceof ReturnStmt) {
      v.visitReturn((ReturnStmt) this);
    } else if (this instanceof StoreStmt) {
      v.visitStore((StoreStmt) this);
    } else if (this instanceof VarExpr) {
      v.visitVar((VarExpr) this);
    } else if (this instanceof InvokeExpr) {
      v.visitInvoke((InvokeExpr) this);
    } else if (this instanceof CastExpr) {
      v.visitCast((CastExpr) this);
    }
  }

  public String toString() {
    return toString(1);
  }

  public String toString(int i) {
    StringBuilder sb = new StringBuilder();
    sb.append(Printing.toString(instruction));
    for (Expr child : this) {
      sb.append('\n');
      sb.append("  ".repeat(Math.max(0, i)));
      sb.append(child.toString(i + 1));
    }
    return sb.toString();
  }
}
