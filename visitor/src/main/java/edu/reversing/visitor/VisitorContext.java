package edu.reversing.visitor;

import com.google.inject.Inject;
import com.google.inject.Injector;
import edu.reversing.asm.tree.data.Hierarchy;
import edu.reversing.asm.tree.data.Library;
import edu.reversing.visitor.convention.OverrideVisitor;
import edu.reversing.visitor.expr.PrintExprTest;
import edu.reversing.visitor.flow.FlowVisitor;
import edu.reversing.visitor.redundancy.AccessVisitor;
import edu.reversing.visitor.redundancy.TryCatchVisitor;

import java.util.ArrayDeque;
import java.util.Deque;

public class VisitorContext {

    private final Deque<Visitor> visitors;

    private final Library library;
    private final Hierarchy hierarchy;

    @Inject
    public VisitorContext(Library library, Hierarchy hierarchy) {
        this.visitors = new ArrayDeque<>();
        this.library = library;
        this.hierarchy = hierarchy;
    }

    public Library getLibrary() {
        return library;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    public void addFirst(Visitor visitor) {
        visitors.addFirst(visitor);
    }

    public void transform() {
        while (!visitors.isEmpty()) {
            Visitor visitor = visitors.pop();
            visitor.transform();
        }
    }

    public void inject(Injector injector) {
        addFirst(injector.getInstance(PrintExprTest.class));
        addFirst(injector.getInstance(FlowVisitor.class));
        addFirst(injector.getInstance(TryCatchVisitor.class));
        addFirst(injector.getInstance(OverrideVisitor.class));
        addFirst(injector.getInstance(AccessVisitor.class));
    }
}
