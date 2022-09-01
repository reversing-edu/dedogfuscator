package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.context.Multiplier;

import java.util.HashMap;
import java.util.Map;

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
        //pull encoders and decoders from identifier, create Multiplier object from them
        //multiplier.getProduct multiplies encoder * decoder
        //if its 1 its a valid multiplier
        //-1 means 1 of the operands is negated
        //other number means constant folded?
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {

    }
}
