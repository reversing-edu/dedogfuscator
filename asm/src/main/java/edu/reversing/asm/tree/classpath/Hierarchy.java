package edu.reversing.asm.tree.classpath;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

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

    public List<ClassNode> getImmediateChildren(String root) {
        return children.getOrDefault(root, Collections.emptyList());
    }

    public List<ClassNode> getImmediateParents(String root) {
        return parents.getOrDefault(root, Collections.emptyList());
    }

    private void visitHierarchy(
            Set<String> visited,
            String root,
            Consumer<ClassNode> visitor,
            Function<String, List<ClassNode>> provider) {
        if (visited.contains(root)) {
            return;
        }

        for (ClassNode cn : provider.apply(root)) {
            visitor.accept(cn);
            visited.add(root);
            visitHierarchy(visited, cn.name, visitor, provider);
        }
    }

    public void visitChildrenOf(String root, Consumer<ClassNode> visitor) {
        visitHierarchy(new HashSet<>(), root, visitor, this::getImmediateChildren);
    }

    public void visitParentsOf(String root, Consumer<ClassNode> visitor) {
        visitHierarchy(new HashSet<>(), root, visitor, this::getImmediateParents);
    }

    public void visitAll(String root, Consumer<ClassNode> visitor) {
        visitChildrenOf(root, visitor);
        visitParentsOf(root, visitor);
    }

    public Set<ClassNode> getParents(String root) {
        Set<ClassNode> parents = new HashSet<>();
        visitParentsOf(root, parents::add);
        return parents;
    }

    public Set<ClassNode> getChildren(String root) {
        Set<ClassNode> children = new HashSet<>();
        visitChildrenOf(root, children::add);
        return children;
    }

    public boolean isOverriden(String owner, String name, String desc) {
        for (ClassNode parentCls : getParents(owner)) {
            for (MethodNode parentMethod : parentCls.methods) {
                if (parentMethod.name.equals(name)
                        && parentMethod.desc.equals(desc)
                        && (parentMethod.access & ACC_STATIC) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean extendsFrom(String cls, String check) {
        return getParents(cls).stream().anyMatch(x -> x.name.equals(check));
    }

    public boolean isOverriden(MethodNode method) {
        return isOverriden(method.getOwner(), method.name, method.desc);
    }

    public boolean isOverriden(MethodInsnNode method) {
        return isOverriden(method.owner, method.name, method.desc);
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
            cls.external = true;
            ClassReader reader = new ClassReader(name);
            reader.accept(cls, ClassReader.SKIP_CODE);
            defined.put(name, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cls;
    }
}