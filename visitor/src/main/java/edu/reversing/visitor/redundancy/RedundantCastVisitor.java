package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Currently handled cases
 * (CAST) null; -> null;
 *
 * Required cases
 * soon bro chill out man
 */
public class RedundantCastVisitor  extends Visitor {

    private int removed = 0;

    @Inject
    protected RedundantCastVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() != CHECKCAST) {
                continue;
            }
            TypeInsnNode tin = (TypeInsnNode) instruction;
            tin.getPrevious().getOpcode();
            if (tin.getPrevious().getOpcode() == ACONST_NULL) {
                removed++;
                method.instructions.remove(instruction);
            }
        }
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Removed ");
        output.append(removed);
        output.append(" redundant casts");
    }
}
