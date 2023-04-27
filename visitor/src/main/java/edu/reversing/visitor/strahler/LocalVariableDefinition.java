package edu.reversing.visitor.strahler;

import edu.reversing.asm.tree.flow.BasicBlock;

import java.util.ArrayList;
import java.util.List;

public class LocalVariableDefinition {

  private final int index;
  private final List<BasicBlock> reads;
  private final List<BasicBlock> writes;

  public LocalVariableDefinition(int index) {
    this.index = index;
    this.reads = new ArrayList<>();
    this.writes = new ArrayList<>();
  }

  public int getIndex() {
    return index;
  }

  public List<BasicBlock> getReads() {
    return reads;
  }

  public List<BasicBlock> getWrites() {
    return writes;
  }
}
