package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ClassNode;
import edu.reversing.asm.tree.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

public class PrintExprTest extends Visitor {

    @Inject
    public PrintExprTest(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        System.out.println("Tree: " + cls.name + "." + method.name + method.desc);
        System.out.println(method.getExprTree(true));
        System.out.println();
    }
}
