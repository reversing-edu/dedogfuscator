package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.asm.tree.*;

import java.lang.reflect.Modifier;

public class AccessVisitor extends Visitor {

    @Inject
    public AccessVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitField(ClassNode cls, FieldNode field) {
        field.access = transform(field.access, false);
    }

    @Override
    public void preVisitMethod(ClassNode cls, MethodNode method) {
        method.access = transform(method.access, true);
    }

    private int transform(int modifier, boolean unfinal) {
        if ((modifier & ACC_PUBLIC) == 0) {
            if ((modifier & ACC_PROTECTED) > 0) {
                modifier &= ~ACC_PROTECTED;
            }

            if ((modifier & ACC_PRIVATE) > 0) {
                modifier &= ~ACC_PRIVATE;
            }

            modifier |= Modifier.PUBLIC;
        }

        if (unfinal && (modifier & ACC_FINAL) > 0 && (modifier & ACC_STATIC) > 0) {
            modifier &= ~ACC_FINAL;
        }

        return modifier;
    }
}
