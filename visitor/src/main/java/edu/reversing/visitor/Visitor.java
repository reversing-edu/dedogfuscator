package edu.reversing.visitor;

import edu.reversing.asm.Library;
import org.objectweb.asm.tree.*;

public abstract non-sealed class Visitor extends VisitorBase {

    protected final VisitorContext context;
    protected final Library library;

    protected Visitor(VisitorContext context, Library library) {
        this.library = library;
        this.context = context;
    }

    public final void transform() {
        preVisit();
        library.forEach(this::transformClass);
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
