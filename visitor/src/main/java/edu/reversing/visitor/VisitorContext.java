package edu.reversing.visitor;

import edu.reversing.asm.Hierarchy;
import edu.reversing.asm.Library;

import java.util.ArrayDeque;
import java.util.Deque;

//TODO dependency injection
public class VisitorContext {

    private final Deque<Visitor> visitors;

    private final Library library;
    private final Hierarchy hierarchy;

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

    public void add(Visitor visitor) {
        visitors.addFirst(visitor);
    }

    public void transform() {
        while (!visitors.isEmpty()) {
            Visitor visitor = visitors.pop();
            visitor.transform();
        }
    }
}
