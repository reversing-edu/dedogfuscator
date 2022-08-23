package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.JumpExpr;
import edu.reversing.asm.tree.ir.TargetExpr;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.JumpInsnNode;

public class ResolveJumpExprVisitor extends Visitor {

    @Inject
    public ResolveJumpExprVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true).accept(new ExprVisitor() {
            @Override
            public void visitJump(JumpExpr jump) {
                JumpInsnNode instruction = jump.getInstruction();
                TargetExpr target = new TargetExpr(jump.getRoot(), instruction.label, 0, 0);
                jump.setTarget(target);
            }
        });
    }
}
