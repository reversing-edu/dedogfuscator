package edu.reversing.visitor;

import org.objectweb.asm.tree.*;

public abstract non-sealed class Visitor extends VisitorBase {

    protected final VisitorContext context;

    protected Visitor(VisitorContext context) {
        this.context = context;
    }

    public final void transform() {
        preVisit();
        context.getLibrary().forEach(this::transformClass);
        postVisit();
    }

    private void transformClass(ClassNode cls) {
        visitClass(cls);

        for (FieldNode field : cls.fields) {
            visitField(cls, field);
        }

        for (MethodNode method : cls.methods) {
            preVisitMethod(cls, method);
            visitCode(cls, method);
            postVisitMethod(cls, method);
        }
    }
}
