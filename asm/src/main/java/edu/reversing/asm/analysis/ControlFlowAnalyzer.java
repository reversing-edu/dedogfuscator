package edu.reversing.asm.analysis;

import edu.reversing.asm.tree.flow.BasicBlock;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowAnalyzer extends Analyzer<BasicValue> {

  private final List<BasicBlock> blocks = new ArrayList<>();

  public ControlFlowAnalyzer() {
    super(new BasicInterpreter());
  }

  @Override
  protected void init(String owner, MethodNode method) {
    BasicBlock block = new BasicBlock(method);
    blocks.add(block);

    for (int i = 0; i < method.instructions.size(); i++) {
      block.setEnd(block.getEnd() + 1);
      AbstractInsnNode instruction = method.instructions.get(i);
      if (instruction.getNext() != null && terminates(instruction)) {
        blocks.add(block = newBasicBlock(method, i + 1));
      }
    }
  }

  private BasicBlock newBasicBlock(MethodNode method, int index) {
    BasicBlock block = new BasicBlock(method);
    block.setStart(index);
    block.setEnd(index);
    return block;
  }

  private BasicBlock getBlock(int instructionIndex) {
    for (BasicBlock block : blocks) {
      if (block.contains(instructionIndex)) {
        return block;
      }
    }

    return null;
  }

  @Override
  protected void newControlFlowEdge(int insnIndex, int successorIndex) {
    BasicBlock current = getBlock(insnIndex);
    BasicBlock successor = getBlock(successorIndex);
    if (current == successor
        || current == null
        || successor == null) {
      return;
    }

    if (successorIndex == insnIndex + 1) {
      current.setSuccessor(successor);
      successor.setPredecessor(current);
      return;
    }

    current.getChildren().add(successor);
  }

  private boolean terminates(AbstractInsnNode instruction) {
    return instruction instanceof LabelNode
           || instruction instanceof JumpInsnNode
           || instruction instanceof TableSwitchInsnNode
           || instruction instanceof LookupSwitchInsnNode
           || instruction.getOpcode() == ATHROW
           || (instruction.getOpcode() >= IRETURN && instruction.getOpcode() <= RETURN);
  }

  public List<BasicBlock> getBlocks() {
    return blocks;
  }
}
