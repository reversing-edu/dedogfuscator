package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ArithmeticExpr extends Expr {

    public ArithmeticExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public Expr getLeft() {
        return get(0);
    }

    public Expr getRight() {
        if (isUnary()) {
            return null;
        }
        return get(1);
    }

    //TODO should split this into UnaryArtihmetic?
    public boolean isUnary() {
        int opcode = getOpcode();
        return opcode == INEG
                || opcode == LNEG
                || opcode == DNEG
                || opcode == FNEG;
    }
}
