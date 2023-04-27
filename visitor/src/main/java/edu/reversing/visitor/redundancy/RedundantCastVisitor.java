package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.CastExpr;
import edu.reversing.asm.tree.ir.Expr;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

/**
 * Currently handled cases
 * (CAST) null; -> null;
 * <p>
 * Required cases
 * soon bro chill out man
 */
public class RedundantCastVisitor extends Visitor {

  private int removed = 0;

  @Inject
  protected RedundantCastVisitor(VisitorContext context) {
    super(context);
  }

  @Override
  public void visitCode(ClassNode cls, MethodNode method) {
    method.getExprTree(true).accept(new ExprVisitor() {
      @Override
      public void visitCast(CastExpr cast) {
        if (cast.getOpcode() != CHECKCAST) {
          return;
        }

        Expr target = cast.get(0);
        if (target.getOpcode() != ACONST_NULL) {
          return;
        }

        removed++;
        method.instructions.remove(cast.getInstruction());
      }
    });
  }

  @Override
  public void output(StringBuilder output) {
    output.append("Removed ");
    output.append(removed);
    output.append(" null casts");
  }
}
