package edu.reversing.asm.tree.ir;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.function.Predicate;

public class InvokeExpr extends Expr {

  private final Type[] argTypes;

  private final Type returnType;
  private final String owner;
  private final String name;

  public InvokeExpr(ExprTree tree, AbstractInsnNode instruction, int consume, int produce) {
    super(tree, instruction, consume, produce);

    MethodInsnNode method = (MethodInsnNode) instruction;

    this.argTypes = Type.getArgumentTypes(method.desc);
    this.returnType = Type.getReturnType(method.desc);
    this.owner = method.owner;
    this.name = method.name;
  }

  public List<Expr> getArgs() {
    return this;
  }

  @Override
  public MethodInsnNode getInstruction() {
    return (MethodInsnNode) super.getInstruction();
  }

  public String key() {
    return owner + "." + name + getInstruction().desc;
  }

  public Type[] getArgTypes() {
    return argTypes;
  }

  public Type getReturnType() {
    return returnType;
  }

  public String getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public boolean isStatic() {
    return getOpcode() == INVOKESTATIC;
  }

  public boolean isReturning(Predicate<Type> predicate) {
    return predicate.test(getReturnType());
  }

  public boolean isReturning(String descriptor) {
    return isReturning(type -> type.getDescriptor().equals(descriptor));
  }

  public boolean containsArg(Predicate<Type> predicate) {
    for (Type type : argTypes) {
      if (predicate.test(type)) {
        return true;
      }
    }
    return false;
  }

  public boolean containsArg(String argDesc) {
    return containsArg(type -> type.getDescriptor().equals(argDesc));
  }
}
