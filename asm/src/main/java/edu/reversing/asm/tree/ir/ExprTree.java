package edu.reversing.asm.tree.ir;

import edu.reversing.asm.commons.Instructions;
import edu.reversing.asm.tree.element.MethodNode;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

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

    public MethodNode getMethod() {
        return method;
    }

    public AbstractInsnNode[] collapse() {
        AbstractInsnNode[] collapsed = super.collapse();
        int offset = collapsed.length > 1 && collapsed[collapsed.length - 2].getType() == AbstractInsnNode.LABEL ? 2 : 1;
        return Arrays.copyOf(collapsed, collapsed.length - offset);
    }

    public void build() {
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
                        case GETSTATIC -> produce = d == 'D' || d == 'J' ? 2 : 1;
                        case GETFIELD -> {
                            consume = 1;
                            produce = d == 'D' || d == 'J' ? 2 : 1;
                        }
                        case PUTSTATIC -> consume = d == 'D' || d == 'J' ? 2 : 1;
                        case PUTFIELD -> consume = d == 'D' || d == 'J' ? 3 : 2;
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

            exprs[i] = getExpr(instruction, opcode, consume, produce);
        }

        ptr = instructions.length - 1;

        for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
            AbstractInsnNode start = Instructions.seek(tcb.start, true);
            AbstractInsnNode end = Instructions.seek(tcb.end, true);
            AbstractInsnNode handler = Instructions.seek(tcb.handler, true);
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

    private Expr getExpr(AbstractInsnNode instruction, int opcode, int consume, int produce) {
        //TODO ew
        //missing cast insns

        if (opcode == BIPUSH || opcode == SIPUSH || (opcode >= ICONST_M1 && opcode <= DCONST_1)) {
            return new NumberExpr(this, instruction, consume, produce);
        }

        if (opcode >= IRETURN && opcode <= RETURN) {
            return new ReturnStmt(this, instruction, consume, produce);
        }

        if (opcode == ARRAYLENGTH) {
            return new ArrayLengthExpr(this, instruction, consume, produce);
        }

        if (opcode >= IALOAD && opcode <= SALOAD) {
            return new ArrayLoadExpr(this, instruction, consume, produce);
        }

        if (opcode >= IASTORE && opcode <= SASTORE) {
            return new ArrayStoreStmt(this, instruction, consume, produce);
        }

        if (opcode >= ILOAD && opcode <= ALOAD) {
            return new VarExpr(this, instruction, consume, produce);
        }

        if (opcode >= ISTORE && opcode <= ASTORE) {
            return new StoreStmt(this, instruction, consume, produce);
        }

        if (opcode >= IADD && opcode <= LXOR) {
            return new ArithmeticExpr(this, instruction, consume, produce);
        }

        if (instruction instanceof LdcInsnNode ldc) {
            if (ldc.cst instanceof Number) {
                return new NumberExpr(this, instruction, consume, produce);
            }
            return new Expr(this, instruction, consume, produce);
        }

        if (instruction instanceof IincInsnNode) {
            return new IncrementExpr(this, instruction, consume, produce);
        }

        if (instruction instanceof LabelNode) {
            return new TargetExpr(this, instruction, consume, produce);
        }

        if (instruction instanceof FieldInsnNode) {
            return new FieldExpr(this, instruction, consume, produce);
        }

        if (instruction instanceof MethodInsnNode) {
            return new InvokeExpr(this, instruction, consume, produce);
        }

        if (instruction instanceof IntInsnNode) {
            return new NumberExpr(this, instruction, consume, produce);
        }

        if (opcode >= IFEQ && opcode <= IFLE) {
            return new UnaryJumpStmt(this, instruction, consume, produce);
        }

        if (opcode >= IF_ICMPEQ && opcode <= IF_ACMPNE) {
            return new BinaryJumpStmt(this, instruction, consume, produce);
        }

        if (opcode == GOTO) {
            return new GotoExpr(this, instruction, consume, produce);
        }

        //remaining ones are jsr ret etc
        if (instruction instanceof JumpInsnNode) {
            return new JumpExpr(this, instruction, consume, produce);
        }

        return new Expr(this, instruction, consume, produce);
    }

    private Expr seek() {
        if (ptr < 0) {
            return null;
        }

        Expr expr = exprs[ptr--];
        if (expr.consume == 0) {
            return expr;
        }

        int consume = expr.consume;
        while (consume != 0) {
            Expr child = seek();
            if (child == null) {
                break;
            }

            if (child.getOpcode() == MONITOREXIT && expr.getOpcode() == ATHROW) {
                child.produce = 1;
            }

            expr.addFirst(child);

            int delta = consume - child.produce;
            if (delta < 0) {
                expr.produce -= delta;
                child.produce = 0;
                break;
            }

            consume -= child.produce;
            child.produce = 0;
        }

        return expr;
    }

    public void accept(ExprVisitor v) {
        for (Expr expr : this) {
            accept(v, expr);
        }
    }

    private void accept(ExprVisitor v, Expr expr) {
        expr.accept(v);
        for (Expr node : expr) {
            accept(v, node);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Expr child : this) {
            builder.append(child);
            builder.append('\n');
        }
        return builder.toString().trim();
    }
}
