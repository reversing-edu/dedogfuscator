package edu.reversing.visitor;

import edu.reversing.asm.Hierarchy;
import edu.reversing.asm.Library;

public class VisitorContext {

    //TODO dependency injection
    private final Library library;
    private final Hierarchy hierarchy;

    public VisitorContext(Library library, Hierarchy hierarchy) {
        this.library = library;
        this.hierarchy = hierarchy;
    }

    public Library getLibrary() {
        return library;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }
}
