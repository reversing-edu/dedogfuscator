package edu.reversing.visitor.expr.multiplier.context;

import edu.reversing.asm.tree.ir.ArithmeticExpr;

import java.util.HashMap;
import java.util.Map;

public class DecodeContext {

    private final Map<ArithmeticExpr, Modulus> expressions;

    public DecodeContext() {
        this.expressions = new HashMap<>();
    }

    public Map<ArithmeticExpr, Modulus> getExpressions() {
        return expressions;
    }
}
