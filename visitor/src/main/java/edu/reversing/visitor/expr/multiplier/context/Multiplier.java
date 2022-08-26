package edu.reversing.visitor.expr.multiplier.context;

import java.math.BigInteger;

public class Multiplier {

    private final String fieldKey;
    private final boolean longType;
    private final Modulus decoder;
    private final BigInteger encoder;

    public Multiplier(String fieldKey, boolean longType, Modulus decoder, BigInteger encoder) {
        this.fieldKey = fieldKey;
        this.longType = longType;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public Modulus getDecoder() {
        return decoder;
    }

    public BigInteger getEncoder() {
        return encoder;
    }

    public boolean validate() {
        BigInteger quotient = decoder.getQuotient();
        long decoder = longType ? quotient.longValue() : quotient.intValue();
        long encoder = longType ? this.encoder.longValue() : this.encoder.intValue();
        return decoder * encoder == 1;
    }
}
