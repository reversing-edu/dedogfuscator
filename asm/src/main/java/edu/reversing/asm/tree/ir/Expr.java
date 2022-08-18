package edu.reversing.asm.tree.ir;

import edu.reversing.asm.commons.Printing;
import edu.reversing.asm.tree.ir.stmt.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.commons.Tree;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

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
        int size = size();
        Expr[] exprs = new Expr[size];
        int i = 0;
        for (Expr child : this) {
            if (child.produce > 0) {
                exprs[i++] = child;
            }
        }
        return exprs;
    }

    public AbstractInsnNode[] getDeepChildren() {
        AbstractInsnNode[] instructions = new AbstractInsnNode[getCumulativeSize()];

        int i = 0;
        for (Expr child : this) {
            AbstractInsnNode[] sub = child.getDeepChildren();
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

    public void accept(ExprVisitor v) {
        v.visitAny(this);

        if (this instanceof ArithmeticExpr) {
            v.visitOperation((ArithmeticExpr) this);
        } else if (this instanceof ArrayLoadExpr) {
            v.visitArrayLoad((ArrayLoadExpr) this);
        } else if (this instanceof ArrayStoreExpr) {
            v.visitArrayStore((ArrayStoreExpr) this);
        } else if (this instanceof FieldExpr) {
            v.visitField((FieldExpr) this);
        } else if (this instanceof IncrementExpr) {
            v.visitIncrement((IncrementExpr) this);
        } else if (this instanceof JumpExpr) {
            v.visitJump((JumpExpr) this);
        } else if (this instanceof NumberExpr) {
            v.visitNumber((NumberExpr) this);
        } else if (this instanceof ReturnExpr) {
            v.visitReturn((ReturnExpr) this);
        } else if (this instanceof StoreExpr) {
            v.visitStore((StoreExpr) this);
        } else if (this instanceof VarExpr) {
            v.visitVar((VarExpr) this);
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
