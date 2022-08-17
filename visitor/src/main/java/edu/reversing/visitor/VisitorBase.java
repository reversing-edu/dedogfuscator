package edu.reversing.visitor;

import edu.reversing.asm.tree.element.*;
import org.objectweb.asm.Opcodes;

public abstract sealed class VisitorBase implements Opcodes permits Visitor {

    public void preVisit() {

    }

    public void visitClass(ClassNode cls) {

    }

    public void visitField(ClassNode cls, FieldNode field) {

    }

    public void preVisitMethod(ClassNode cls, MethodNode method) {

    }

    public void visitCode(ClassNode cls, MethodNode method) {

    }

    public void postVisitMethod(ClassNode cls, MethodNode method) {

    }

    public void postVisit() {

    }
}
