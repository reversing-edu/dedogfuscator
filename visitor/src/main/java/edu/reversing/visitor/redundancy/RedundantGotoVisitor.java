package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

public class RedundantGotoVisitor extends Visitor {

    private int removed = 0;

    @Inject
    public RedundantGotoVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() != GOTO) {
                continue;
            }

            JumpInsnNode jump = (JumpInsnNode) instruction;
            AbstractInsnNode next = jump.getNext();
            if (next == null || next.getType() != AbstractInsnNode.LABEL) {
                continue;
            }

            if (jump.label == next) {
                method.instructions.remove(instruction);
                removed++;
            }
        }
    }

    @Override
    public void postVisit() {
        System.out.println("Inlined " + removed + " GOTO instructions");
    }
}
