package edu.reversing.visitor.convention;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

public class OverrideVisitor extends Visitor {

    private static final String DESCRIPTOR = Type.getDescriptor(Override.class);

    private int inserted = 0;

    @Inject
    public OverrideVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void preVisitMethod(ClassNode cls, MethodNode method) {
        //constructors and static methods should not have @Override annotation
        if (method.name.equals("<init>")
                || method.name.equals("<clinit>")
                || (method.access & ACC_STATIC) > 0) {
            return;
        }

        //method is not overriden
        if (!context.getHierarchy().isOverriden(method)) {
            return;
        }

        //method already has @Override annotation
        if (method.invisibleAnnotations != null) {
            for (AnnotationNode annotation : method.invisibleAnnotations) {
                if (annotation.desc.equals(DESCRIPTOR)) {
                    return;
                }
            }
        }

        if (method.invisibleAnnotations == null) {
            method.invisibleAnnotations = List.of(new AnnotationNode(DESCRIPTOR));
        }

        inserted++;
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Inserted ");
        output.append(inserted);
        output.append(" @Override annotations");
    }
}
