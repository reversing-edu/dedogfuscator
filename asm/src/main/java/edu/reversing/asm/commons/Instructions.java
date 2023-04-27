package edu.reversing.asm.commons;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Instructions {

  public static AbstractInsnNode seek(AbstractInsnNode instruction, boolean next) {
    while (instruction != null && instruction.getOpcode() == -1) {
      instruction = next ? instruction.getNext() : instruction.getPrevious();
    }
    return instruction;
  }
}
