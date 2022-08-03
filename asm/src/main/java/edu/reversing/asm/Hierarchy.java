package edu.reversing.asm;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ClassNode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.function.Consumer;

public class Hierarchy {

    private final Library library;

    private final Map<String, ClassNode> defined = new HashMap<>();

    private final Map<String, List<ClassNode>> children = new HashMap<>();
    private final Map<String, List<ClassNode>> parents = new HashMap<>();

    @Inject
    public Hierarchy(Library library) {
        this.library = library;

        Deque<String> pending = new ArrayDeque<>();
        for (ClassNode cls : library) {
            pending.push(cls.name);
        }

        while (pending.size() > 0) {
            String current = pending.pop();
            ClassNode cls = defineClass(current);
            if (cls != null && cls.superName != null) {
                visit(cls);
                pending.push(cls.superName);
                cls.interfaces.forEach(pending::push);
            }
        }
    }

    public List<ClassNode> getChildren(String root) {
        return children.getOrDefault(root, Collections.emptyList());
    }

    public List<ClassNode> getParents(String root) {
        return parents.getOrDefault(root, Collections.emptyList());
    }

    public void visitChildrenOf(String root, Consumer<ClassNode> function) {
        for (ClassNode cn : getChildren(root)) {
            function.accept(cn);
        }
    }

    public void visitParentsOf(String root, Consumer<ClassNode> function) {
        for (ClassNode cn : getParents(root)) {
            function.accept(cn);
        }
    }

    private void visit(ClassNode cls) {
        visitSuper(cls, cls.superName);
        for (String itf : cls.interfaces) {
            visitSuper(cls, itf);
        }
    }

    private void visitSuper(ClassNode cls, String superName) {
        ClassNode parent = superName == null ? null : defineClass(superName);
        if (parent != null && parent.name.equals(Type.getInternalName(Object.class))) {
            return;
        }

        parents.computeIfAbsent(cls.name, n -> new ArrayList<>()).add(parent);
        if (parent != null) {
            List<ClassNode> parentChildren = children.computeIfAbsent(parent.name, n -> new ArrayList<>());
            if (!parentChildren.contains(cls)) {
                parentChildren.add(cls);
            }
        }
    }

    private ClassNode defineClass(String name) {
        ClassNode cls = defined.get(name);
        if (cls != null || (cls = library.lookup(name)) != null) {
            return cls;
        }

        try {
            cls = new ClassNode();
            ClassReader reader = new ClassReader(name);
            reader.accept(cls, ClassReader.SKIP_CODE);
            defined.put(name, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cls;
    }
}