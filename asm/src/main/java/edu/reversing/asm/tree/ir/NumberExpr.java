package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.*;

public class NumberExpr extends Expr {

    public NumberExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
    }

    public long getValue() {
        AbstractInsnNode instruction = getInstruction();
        int op = instruction.getOpcode();
        switch (op) {
            case NEWARRAY, BIPUSH, SIPUSH -> {
                return ((IntInsnNode) instruction).operand;
            }

            case ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> {
                return op - ICONST_0;
            }

            case LCONST_0, LCONST_1 -> {
                return op - LCONST_0;
            }

            case FCONST_0, FCONST_1, FCONST_2 -> {
                return op - FCONST_0;
            }

            case DCONST_0, DCONST_1 -> {
                return op - DCONST_0;
            }

            case LDC -> {
                Object cst = ((LdcInsnNode) instruction).cst;
                if (cst instanceof Integer) {
                    return (Integer) cst;
                } else if (cst instanceof Long) {
                    return (Long) cst;
                }
            }
        }

        return -1;
    }

    public double getFloatingPointValue() {
        AbstractInsnNode instruction = getInstruction();
        int op = instruction.getOpcode();
        switch (op) {
            case FCONST_0, FCONST_1, FCONST_2 -> {
                return op - FCONST_0;
            }

            case DCONST_0, DCONST_1 -> {
                return op - DCONST_0;
            }

            default -> {
                return -1;
            }
        }
    }
}
