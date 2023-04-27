package edu.reversing.asm.tree.ir;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.function.Predicate;

//TODO should this be FieldRefExpr nad FieldStoreExpr?
public class FieldExpr extends Expr {

  private final Type type;

  private final String owner;
  private final String name;

  public FieldExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);

    FieldInsnNode field = (FieldInsnNode) instruction;

    this.type = Type.getType(field.desc);
    this.owner = field.owner;
    this.name = field.name;
  }

  public FieldInsnNode getInstruction() {
    return (FieldInsnNode) super.getInstruction();
  }

  public boolean isStatic() {
    int op = getOpcode();
    return op == GETSTATIC || op == PUTSTATIC;
  }

  public boolean isGetting() {
    int op = getOpcode();
    return op == GETSTATIC || op == GETFIELD;
  }

  public boolean isSetting() {
    int op = getOpcode();
    return op == PUTSTATIC || op == PUTFIELD;
  }

  public String key() {
    return owner + "." + name;
  }

  public Type getType() {
    return type;
  }

  public String getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public boolean isType(Predicate<Type> predicate) {
    return predicate.test(type);
  }

  public boolean isType(String descriptor) {
    return isType(type -> type.getDescriptor().equals(descriptor));
  }
}
