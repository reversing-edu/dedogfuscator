package edu.reversing.asm.tree.ir;

public class TryCatchMeta {

  private final boolean start;
  private final boolean end;
  private final boolean handler;

  public TryCatchMeta(boolean start, boolean end, boolean handler) {
    this.start = start;
    this.end = end;
    this.handler = handler;
  }

  public boolean isStart() {
    return start;
  }

  public boolean isEnd() {
    return end;
  }

  public boolean isHandler() {
    return handler;
  }
}
