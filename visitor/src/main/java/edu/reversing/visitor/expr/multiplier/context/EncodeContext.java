package edu.reversing.visitor.expr.multiplier.context;

import edu.reversing.asm.tree.ir.FieldExpr;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class EncodeContext {

  private final Map<FieldExpr, BigInteger> expressions;

  public EncodeContext() {
    this.expressions = new HashMap<>();
  }

  public Map<FieldExpr, BigInteger> getExpressions() {
    return expressions;
  }
}
