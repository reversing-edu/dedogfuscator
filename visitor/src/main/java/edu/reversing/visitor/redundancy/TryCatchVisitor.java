package edu.reversing.visitor.redundancy;

import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class TryCatchVisitor extends Visitor {

    private int removed = 0;

    public TryCatchVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        List<TryCatchBlockNode> runtime = new ArrayList<>();
        for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
            if (tcb.type != null && tcb.type.contains("Runtime")) {
                runtime.add(tcb);
            }
        }

        List<TryCatchBlockNode> skip = new ArrayList<>();
        for (TryCatchBlockNode tcb : runtime) {
            if (skip.contains(tcb)) {
                method.tryCatchBlocks.remove(tcb);
                continue;
            }

            List<TryCatchBlockNode> overlapping = new ArrayList<>();
            for (TryCatchBlockNode check : runtime) {
                if (check != tcb && check.handler == tcb.handler) {
                    overlapping.add(check);
                }
            }
            skip.addAll(overlapping);

            AbstractInsnNode terminator = tcb.handler.getNext();
            while (!terminates(terminator.getOpcode())) {
                terminator = terminator.getNext();
            }

            if (terminator.getOpcode() == ATHROW) {
                terminator = tcb.handler.getNext();
                while (!terminates(terminator.getOpcode())) {
                    AbstractInsnNode temp = terminator;
                    terminator = terminator.getNext();
                    method.instructions.remove(temp);
                }

                method.instructions.remove(terminator);
                method.tryCatchBlocks.remove(tcb);
                removed++;
            }
        }
    }

    private boolean terminates(int opcode) {
        return (opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW;
    }

    @Override
    public void postVisit() {
        System.out.println("Removed " + removed + " try catch blocks");
    }
}
