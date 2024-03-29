package edu.reversing.visitor.expr.multiplier;

import org.objectweb.asm.Type;

import java.math.BigInteger;

public class Modulus {

  private final BigInteger quotient;
  private final int bits;

  public Modulus(BigInteger quotient, int bits) {
    this.quotient = quotient;
    this.bits = bits;
  }

  public Modulus(BigInteger quotient, boolean longType) {
    this(quotient, longType ? 64 : 32);
  }

  public Modulus(BigInteger quotient, Type type) {
    this(quotient, type.getDescriptor().equals("J"));
  }

  public BigInteger inverse() {
    try {
      BigInteger shift = BigInteger.ONE.shiftLeft(bits);
      return quotient.modInverse(shift);
    } catch (ArithmeticException e) {
      return null;
    }
  }

  public boolean validate() {
    return inverse() != null;
  }

  public int getBits() {
    return bits;
  }

  public BigInteger getQuotient() {
    return quotient;
  }
}