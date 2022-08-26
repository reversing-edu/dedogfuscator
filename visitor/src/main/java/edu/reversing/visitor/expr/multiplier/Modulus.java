package edu.reversing.visitor.expr.multiplier;

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

    public Modulus(BigInteger quotient, String desc) {
        this(quotient, desc.equals("J"));
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

    public BigInteger getQuotient() {
        return quotient;
    }
}