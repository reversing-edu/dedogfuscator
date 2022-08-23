package edu.reversing.asm.tree.ir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;

public class ArithmeticExpr extends Expr {

    private final Operation operation;

    public ArithmeticExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
        super(tree, instruction, consume, produce);
        operation = Operation.valueOf(instruction.getOpcode());
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

    public boolean isUnary() {
        int opcode = getOpcode();
        return opcode == INEG
                || opcode == LNEG
                || opcode == DNEG
                || opcode == FNEG;
    }

    public Operation getOperation() {
        return operation;
    }

    public enum Operation {

        ADD,
        SUB,
        MUL,
        DIV,
        REM,
        NEG,
        SHIFT,
        AND,
        OR,
        XOR;

        public static Operation valueOf(int opcode) {
            switch (opcode) {
                case IADD, LADD, FADD, DADD -> {
                    return ADD;
                }

                case ISUB, LSUB, FSUB, DSUB -> {
                    return SUB;
                }

                case IMUL, LMUL, FMUL, DMUL -> {
                    return MUL;
                }

                case IDIV, LDIV, FDIV, DDIV -> {
                    return DIV;
                }

                case IREM, LREM, FREM, DREM -> {
                    return REM;
                }

                case INEG, LNEG, FNEG, DNEG -> {
                    return NEG;
                }

                case ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR -> {
                    return SHIFT;
                }

                case IAND, LAND -> {
                    return AND;
                }

                case IOR, LOR -> {
                    return OR;
                }

                case IXOR, LXOR -> {
                    return XOR;
                }

                default -> throw new IllegalArgumentException("Invalid Operation: " + Printer.OPCODES[opcode]);
            }
        }
    }
}
