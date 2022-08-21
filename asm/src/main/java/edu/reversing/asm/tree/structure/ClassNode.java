package edu.reversing.asm.tree.structure;

import org.objectweb.asm.*;

import java.util.*;

public class ClassNode extends org.objectweb.asm.tree.ClassNode {

    public final List<FieldNode> fields;
    public final List<MethodNode> methods;

    public boolean external = false;

    public ClassNode() {
        super(Opcodes.ASM9);
        this.fields = new BiList<>(super.fields);
        this.methods = new BiList<>(super.methods);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldNode field = new FieldNode(access, this.name, name, descriptor, signature, value);
        fields.add(field);
        return field;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodNode method = new MethodNode(access, this.name, name, descriptor, signature, exceptions);
        methods.add(method);
        return method;
    }

    public MethodNode getMethod(String name, String desc) {
        for (MethodNode method : methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }

    //still dont like this
    private static class BiList<T> extends ArrayList<T> {

        private final List<? super T> original;

        private BiList(List<? super T> original) {
            this.original = original;
        }

        @Override
        public boolean add(T t) {
            return original.add(t) && super.add(t);
        }

        @Override
        public boolean remove(Object o) {
            return original.remove(o) && super.remove(o);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return original.addAll(c) && super.addAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return original.removeAll(c) && super.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return original.retainAll(c) && super.retainAll(c);
        }
    }
}
