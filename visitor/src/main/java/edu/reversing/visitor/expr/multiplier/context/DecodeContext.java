package edu.reversing.visitor.expr.multiplier.context;

import edu.reversing.asm.tree.ir.ArithmeticExpr;
import edu.reversing.commons.Multiset;
import edu.reversing.visitor.expr.multiplier.Modulus;

import java.math.BigInteger;
import java.util.*;

public class DecodeContext {

  private final Map<ArithmeticExpr, Modulus> expressions;

  public DecodeContext() {
    this.expressions = new HashMap<>();
  }

  public Map<ArithmeticExpr, Modulus> getExpressions() {
    return expressions;
  }

  public Modulus getBest() {
    Map<BigInteger, Modulus> pairs = new HashMap<>();
    Multiset<BigInteger> multiset = new Multiset<>();

    for (Modulus modulus : expressions.values()) {
      pairs.put(modulus.getQuotient(), modulus);
      multiset.add(modulus.getQuotient());
    }

    return pairs.get(multiset.maximum());
  }

  public List<BigInteger> toBigIntegers() {
    return expressions.values().stream().map(Modulus::getQuotient).toList();
  }
}
