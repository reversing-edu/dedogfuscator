package edu.reversing.visitor.convention;

import com.google.inject.Inject;
import edu.reversing.asm.tree.data.Hierarchy;
import edu.reversing.asm.tree.element.ClassNode;
import edu.reversing.asm.tree.element.MethodNode;
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

        Hierarchy hierarchy = context.getHierarchy();
        boolean overriden = false;

        //method is not overriden
        overrideLoop:
        for (ClassNode parentCls : hierarchy.getParents(cls.name)) {
            for (MethodNode parentMethod : parentCls.methods) {
                if (parentMethod.name.equals(method.name)
                        && parentMethod.desc.equals(method.desc)
                        && (parentMethod.access & ACC_STATIC) == 0) {
                    overriden = true;
                    break overrideLoop;
                }
            }
        }

        if (!overriden) {
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
    public void postVisit() {
        System.out.println("Inserted " + inserted + " @Override annotations");
    }
}
