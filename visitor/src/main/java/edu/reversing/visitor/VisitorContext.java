package edu.reversing.visitor;

import com.google.inject.Inject;
import com.google.inject.Injector;
import edu.reversing.asm.tree.classpath.Hierarchy;
import edu.reversing.asm.tree.classpath.Library;
import edu.reversing.visitor.convention.OverrideVisitor;
import edu.reversing.visitor.expr.DupVisitor;
import edu.reversing.visitor.expr.ExprOrderVisitor;
import edu.reversing.visitor.expr.multiplier.MultiplierVisitor;
import edu.reversing.visitor.flow.ControlFlowDFSVisitor;
import edu.reversing.visitor.redundancy.*;
import edu.reversing.visitor.strahler.StrahlerNumberVisitor;

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
        StringBuilder output = new StringBuilder("Transforming classes...\n");
        long start = System.currentTimeMillis();
        while (!visitors.isEmpty()) {
            long visitorStart = System.currentTimeMillis();
            Visitor visitor = visitors.pop();
            visitor.transform();
            output.append("\t");
            visitor.output(output);
            output.append(" (");
            output.append(System.currentTimeMillis() - visitorStart);
            output.append(" ms)\n");
        }
        output.append("Executed transforms in ");
        output.append(System.currentTimeMillis() - start);
        output.append(" ms");
        System.out.println(output);
    }

    public void inject(Injector injector) {
        //code deob
        addFirst(injector.getInstance(StrahlerNumberVisitor.class));
        addFirst(injector.getInstance(OpaquePredicateVisitor.class));

        //ast deob
        //addFirst(injector.getInstance(AddSubNegateVisitor.class)); //need to remove multis first, also idk how safe this is yet
        addFirst(injector.getInstance(MultiplierVisitor.class));
        addFirst(injector.getInstance(ExprOrderVisitor.class));
        addFirst(injector.getInstance(DupVisitor.class));

        //block sorting
        addFirst(injector.getInstance(RedundantGotoVisitor.class));
        addFirst(injector.getInstance(ControlFlowDFSVisitor.class));

        //convention
        addFirst(injector.getInstance(OverrideVisitor.class));

        //redundancy and general cleanup
        addFirst(injector.getInstance(TryCatchVisitor.class));
        addFirst(injector.getInstance(AccessVisitor.class));
        addFirst(injector.getInstance(UnusedMethodVisitor.class));
    }
}
