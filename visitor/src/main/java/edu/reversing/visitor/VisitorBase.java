package edu.reversing.visitor;

import org.objectweb.asm.tree.*;

public abstract sealed class VisitorBase permits Visitor {

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
