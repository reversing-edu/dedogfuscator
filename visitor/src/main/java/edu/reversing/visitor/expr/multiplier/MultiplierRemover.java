package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.context.DecodeContext;
import edu.reversing.visitor.expr.multiplier.context.EncodeContext;

import java.math.BigInteger;
import java.util.*;

public class MultiplierRemover extends Visitor {

  private final Map<String, Multiplier> multipliers = new HashMap<>();

  private final MultiplierIdentifier identifier;

  @Inject
  public MultiplierRemover(VisitorContext context, MultiplierIdentifier identifier) {
    super(context);
    this.identifier = identifier;
  }

  @Override
  public void preVisit() {
    Set<String> keys = new HashSet<>();
    keys.addAll(identifier.getDecoders().keySet());
    keys.addAll(identifier.getEncoders().keySet());
    keys.addAll(identifier.getInvalidatedDecoders().keySet());

    for (String key : keys) {
      DecodeContext decoders = identifier.getDecoders(key);
      EncodeContext encoders = identifier.getEncoders(key);
      if (decoders == null && encoders == null) {
        //no multiplier, maybe we can use invalidated one here?
        continue;
      }

      if (decoders != null && encoders == null) {
        //use inverse
        Modulus best = decoders.getBest();
        if (best == null) {
          best = decoders.getExpressions().values().stream().findAny().orElseThrow();
        }

        multipliers.put(key, new Multiplier(key, best, best.inverse()));
        continue;
      }

      if (decoders != null) {
        //use both
        Collection<Modulus> quotients = decoders.getExpressions().values();
        Collection<BigInteger> products = encoders.getExpressions().values();
        for (Modulus q : quotients) {
          for (BigInteger p : products) {
            if (q.getQuotient().multiply(p).longValue() == 1) {
              multipliers.put(key, new Multiplier(key, q, p));
            }
          }
        }
      }

      DecodeContext invalidated = identifier.getInvalidatedDecoders().get(key);
      if (decoders == null && invalidated != null) {
        //handle constant folded values
      }

      if (decoders != null) {
        //last case.. just inverse as failsafe
        Modulus best = decoders.getBest();
        if (best == null) {
          best = decoders.getExpressions().values().stream().findAny().orElseThrow();
        }

        multipliers.put(key, new Multiplier(key, best, best.inverse()));
      }
    }
  }

  @Override
  public void visitCode(ClassNode cls, MethodNode method) {

  }

  @Override
  public void postVisit() {

  }

  @Override
  public void output(StringBuilder output) {
    output.append("Identified ");
    output.append(multipliers.size());
    output.append(" multiplier pairs (validated)");
  }
}
