package edu.reversing.visitor.expr.multiplier;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.context.Multiplier;

import java.util.HashMap;
import java.util.Map;

public class MultiplierRemover extends Visitor {

    private static final int[] DUPS = {DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2};

    private final Map<String, Multiplier> multipliers = new HashMap<>();

    private final MultiplierIdentifier identifier;

    @Inject
    public MultiplierRemover(VisitorContext context, MultiplierIdentifier identifier) {
        super(context);
        this.identifier = identifier;
    }

    @Override
    public void preVisit() {
        //pull encoders and decoders, create Multiplier object from them
        //if Multiplier.validate add to map
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true).accept(new ExprVisitor() {
            @Override
            public void visitAny(Expr expr) {
                for (int dup : DUPS) {
                    if (expr.getOpcode() == dup && expr.getCumulativeSize() < 20 && expr.getCumulativeSize() > 3) {
                        //  System.out.println(expr.getRoot().getMethod().key());
                        //  System.out.println("par " + Printing.toString(expr.getParent().getInstruction()));
                        //  System.out.println(expr);
                    }
                }
            }
        });
    }
}
