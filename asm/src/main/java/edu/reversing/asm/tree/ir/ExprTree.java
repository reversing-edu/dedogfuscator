package edu.reversing.asm.tree.ir;

import edu.reversing.asm.tree.MethodNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

public class ExprTree extends Expr {

    public static final int[] PRODUCING, CONSUMING;

    static {
        int[] consuming = new int[202];
        int[] producing = new int[202];
        String consume = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACCCCCCCCBCBCBAAA"
                + "AAAAAAAAAAAAAAAAADEDEDDDDBCBCDCDECCECECECECECECECECECEBCBCCDCD"
                + "CDCECECEABBBCCCBBBCCCBBBECCEEBBBBBBCCCCCCCCAAABBBCBCBAAAAAAAAA"
                + "AABBBBBBBBAABBAA";
        String produce = "ABBBBBBBBCCBBBCCBBBAABCBCBAAAAAAAAAAAAAAAAAAAABCBCBBBBAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAACDEEFGCBCBCBCBCBCBCBCBCBCBCBCBCBCBC"
                + "BCBCBCBCACBCBBCBCCBCBBBBBBBBBAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAA"
                + "ABBBBBBBAAAAAAAA";

        Arrays.setAll(consuming, i -> consume.charAt(i) - 'A');
        Arrays.setAll(producing, i -> produce.charAt(i) - 'A');
        PRODUCING = producing;
        CONSUMING = consuming;
    }

    private final MethodNode method;
    private Expr[] exprs;
    private int ptr;

    public ExprTree(MethodNode method) {
        super(null, null, -1, -1);
        this.method = method;
    }

    //TODO this should be in some util class
    private static AbstractInsnNode getNextVirtual(AbstractInsnNode instruction) {
        while (instruction != null && instruction.getOpcode() == -1) {
            instruction = instruction.getNext();
        }
        return instruction;
    }

    public MethodNode getMethod() {
        return method;
    }

    public AbstractInsnNode[] getDeepChildren() {
        AbstractInsnNode[] collapsed = super.getDeepChildren();
        int offset = collapsed.length > 1 && collapsed[collapsed.length - 2].getType() == AbstractInsnNode.LABEL ? 2 : 1;
        return Arrays.copyOf(collapsed, collapsed.length - offset);
    }

    //TODO add other expr types
    public void build(boolean forceNew) {
        if (exprs != null && !forceNew) {
            return;
        }

        List<JumpExpr> jumps = new ArrayList<>();
        AbstractInsnNode[] instructions = method.instructions.toArray();
        exprs = new Expr[instructions.length];

        for (int i = 0; i < instructions.length; i++) {
            AbstractInsnNode instruction = instructions[i];
            int type = instruction.getType();
            int opcode = instruction.getOpcode();
            int consume = 0;
            int produce = 0;
            switch (type) {
                case INSN, INT_INSN, VAR_INSN, TYPE_INSN, JUMP_INSN, TABLESWITCH_INSN, LOOKUPSWITCH_INSN -> {
                    consume = CONSUMING[opcode];
                    produce = PRODUCING[opcode];
                }

                case FIELD_INSN -> {
                    char d = ((FieldInsnNode) instruction).desc.charAt(0);
                    switch (opcode) {
                        case Opcodes.GETSTATIC -> produce = d == 'D' || d == 'J' ? 2 : 1;
                        case Opcodes.GETFIELD -> {
                            consume = 1;
                            produce = d == 'J' || d == 'D' ? 2 : 1;
                        }
                        case Opcodes.PUTSTATIC -> consume = d == 'D' || d == 'J' ? 2 : 1;
                        case Opcodes.PUTFIELD -> consume = d == 'J' || d == 'D' ? 3 : 2;
                    }
                }

                case METHOD_INSN -> {
                    int args = Type.getArgumentsAndReturnSizes(((MethodInsnNode) instruction).desc);
                    if (opcode == INVOKESTATIC || opcode == INVOKEDYNAMIC) {
                        consume = (args >> 2) - 1;
                    } else {
                        consume = args >> 2;
                    }
                    produce = args & 0x3;
                }

                case LDC_INSN -> {
                    Object cst = ((LdcInsnNode) instruction).cst;
                    produce = cst instanceof Long || cst instanceof Double ? 2 : 1;
                }

                case MULTIANEWARRAY_INSN -> {
                    consume = ((MultiANewArrayInsnNode) instruction).dims;
                    produce = 1;
                }

                case LABEL, IINC_INSN, FRAME, LINE -> {

                }

                default -> throw new RuntimeException();
            }

            Expr expr = null;
            if (instruction.getType() == AbstractInsnNode.LDC_INSN) {
                Object cst = ((LdcInsnNode) instruction).cst;
                if (cst instanceof Number) {
                    expr = new Expr(this, instruction, consume, produce);
                }
            } else if (opcode == BIPUSH || opcode == SIPUSH || (opcode >= ICONST_M1 && opcode <= DCONST_1)) {
                expr = new Expr(this, instruction, consume, produce);
            }

            if (opcode >= IRETURN && opcode <= RETURN) {
                expr = new Expr(this, instruction, consume, produce);
            }

            if (expr == null) {
                switch (instruction.getType()) {
                    case FIELD_INSN, METHOD_INSN -> expr = new Expr(this, instruction, consume, produce);

                    case JUMP_INSN -> {
                        JumpExpr jn = new JumpExpr(this, instruction, consume, produce);
                        jumps.add(jn);
                        expr = jn;
                    }

                    default -> expr = new Expr(this, instruction, consume, produce);
                }
            }

            exprs[i] = expr;
        }

        for (int i = 0; i < exprs.length; i++) {
            Expr expr = exprs[i];
            AbstractInsnNode instruction = expr.getInstruction();
            if (instruction.getType() != LABEL) {
                continue;
            }

            TargetExpr target = new TargetExpr(this, instruction, expr.consume, expr.produce);
            boolean targeted = false;
            for (JumpExpr jn : jumps) {
                //TODO this doesnt work, i think need to check next instruction not info
                //info was used in old asm
                if (jn.getInstruction().label.getLabel().info == exprs[i].getInstruction()) {
                    jn.setTarget(target);
                    targeted = true;
                }
            }

            if (targeted) {
                exprs[i] = target;
            } else {
                LabelNode label = new LabelNode(((LabelNode) exprs[i].getInstruction()).getLabel());
                exprs[i].setInstruction(label); //TODO breaks shit
            }
        }

        ptr = instructions.length - 1;

        for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
            AbstractInsnNode start = getNextVirtual(tcb.start);
            AbstractInsnNode end = getNextVirtual(tcb.end);
            AbstractInsnNode handler = getNextVirtual(tcb.handler);
            for (Expr expr : exprs) {
                AbstractInsnNode instruction = expr.getInstruction();
                expr.tryCatchMeta = new TryCatchMeta(instruction == start, instruction == end, instruction == handler);
            }
        }

        while (true) {
            Expr next = seek();
            if (next == null) {
                break;
            }

            addFirst(next);
        }
    }

    private Expr seek() {
        if (ptr < 0) {
            return null;
        }

        Expr expr = exprs[ptr--];
        if (expr.consume == 0) {
            return expr;
        }

        int consume = expr.tryCatchMeta != null && expr.tryCatchMeta.isHandler() ? 0 : expr.consume;
        int remaining = consume;
        while (remaining != 0) {
            Expr next = seek();
            if (next == null) {
                break;
            }

            int opcode = next.getOpcode();
            if (opcode == GOTO || opcode == JSR) {
                JumpExpr jump = (JumpExpr) next;
                if (jump.getTarget() != null && jump.getTarget().resolve() == expr) {
                    remaining = consume;
                }
            } else if (opcode == MONITOREXIT && expr.getOpcode() == ATHROW) {
                next.remaining = 1;
            }

            expr.addFirst(next);
            int delta = remaining - next.remaining;
            if (delta < 0) {
                expr.remaining += -delta;
                next.remaining = 0;
                break;
            }

            remaining -= next.remaining;
            next.remaining = 0;
        }

        return expr;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Expr n : this) {
            builder.append(n);
            builder.append('\n');
        }
        return builder.toString().trim();
    }
}
