package edu.reversing.asm.tree.ir;

import org.objectweb.asm.Type;
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

    ADD(IADD, LADD, FADD, DADD),
    SUB(ISUB, LSUB, FSUB, DSUB),
    MUL(IMUL, LMUL, FMUL, DMUL),
    DIV(IDIV, LDIV, FDIV, DDIV),
    REM(IREM, LREM, FREM, DREM),
    NEG(INEG, LNEG, FNEG, DNEG),
    SHL(ISHL, LSHL),
    SHR(ISHR, LSHR),
    USHR(IUSHR, LUSHR),
    AND(IAND, LAND),
    OR(IOR, LOR),
    XOR(IXOR, LXOR);

    private static final Class<? extends Number>[] CLASSES = new Class[]{int.class, long.class, float.class, double.class};

    private final int[] opcodes;

    Operation(int i, int j, int f, int d) {
      this.opcodes = new int[]{i, j, f, d};
    }

    Operation(int i, int j) {
      this(i, j, -1, -1);
    }

    public static Operation valueOf(int opcode) {
      for (Operation operation : values()) {
        for (int op : operation.opcodes) {
          if (opcode == op) {
            return operation;
          }
        }
      }

      throw new IllegalArgumentException("Unsupported Operation: " + Printer.OPCODES[opcode]);
    }

    public static Class<? extends Number> getType(int opcode) {
      for (Operation operation : Operation.values()) {
        int[] opcodes = operation.opcodes;
        for (int i = 0; i < opcodes.length; i++) {
          if (opcodes[i] == opcode) {
            return CLASSES[i];
          }
        }
      }

      throw new IllegalArgumentException("Unknown type: " + Printer.OPCODES[opcode]);
    }

    public int getOpcode(Type type) {
      return switch (type.getDescriptor()) {
        case "I" -> getOpcode(int.class);
        case "J" -> getOpcode(long.class);
        case "F" -> getOpcode(float.class);
        case "D" -> getOpcode(double.class);
        default -> throw new IllegalArgumentException("Unexpected descriptor: " + type.getDescriptor());
      };
    }

    public int getOpcode(Class<? extends Number> type) {
      if (type == int.class) {
        return opcodes[0];
      } else if (type == long.class) {
        return opcodes[1];
      } else if (type == float.class) {
        return opcodes[2];
      } else if (type == double.class) {
        return opcodes[3];
      }

      throw new IllegalArgumentException("Unsupported type: " + type.getSimpleName());
    }
  }
}
