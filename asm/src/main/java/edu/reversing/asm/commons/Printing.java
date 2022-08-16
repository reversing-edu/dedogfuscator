package edu.reversing.asm.commons;

import edu.reversing.asm.tree.ir.Expr;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

public class Printing {

    public static String toString(AbstractInsnNode instruction) {
        int opcode = instruction.getOpcode();
        if (opcode == -1) {
            return instruction.toString();
        }

        String opname = Printer.OPCODES[opcode];
        StringBuilder builder = new StringBuilder(opname);

        switch (instruction.getType()) {
            case FIELD_INSN -> {
                FieldInsnNode field = (FieldInsnNode) instruction;
                builder.append(' ');
                builder.append(field.owner);
                builder.append('.');
                builder.append(field.name);
                builder.append('(');
                builder.append(field.desc); //prettify desc? e.g Ljava/lang/Object; -> Object or java.lang.Object
                builder.append(')');
            }

            case METHOD_INSN -> {
                MethodInsnNode method = (MethodInsnNode) instruction;
                builder.append(' ');
                builder.append(method.owner);
                builder.append('.');
                builder.append(method.name);
                builder.append(method.desc); //prettify desc? e.g Ljava/lang/Object; -> Object or java.lang.Object
            }

            case INT_INSN -> {
                builder.append(' ');
                builder.append(((IntInsnNode) instruction).operand);
            }

            case VAR_INSN -> {
                builder.append('_');
                builder.append(((VarInsnNode) instruction).var);
            }

            case TYPE_INSN -> {
                builder.append(' ');
                builder.append(((TypeInsnNode) instruction).desc);
            }

            case LDC_INSN -> {
                Object cst = ((LdcInsnNode) instruction).cst;
                builder.append(' ');
                builder.append(cst.getClass().getName());
                builder.append(' ');
                builder.append(cst);
            }

            case IINC_INSN -> {
                IincInsnNode increment = (IincInsnNode) instruction;
                builder.append('_');
                builder.append(increment.var);
                builder.append(' ');
                builder.append(increment.incr);
            }

            case MULTIANEWARRAY_INSN -> {
                MultiANewArrayInsnNode multianewarray = (MultiANewArrayInsnNode) instruction;
                builder.append(' ');
                builder.append(multianewarray.desc);
                builder.append(' ');
                builder.append(multianewarray.dims);
            }

            case JUMP_INSN -> {
                JumpInsnNode jump = (JumpInsnNode) instruction;
                builder.append(' ');
                builder.append(jump.label);
            }

            case TABLESWITCH_INSN, LOOKUPSWITCH_INSN -> {

            }
        }

        return builder.toString();
    }

}
