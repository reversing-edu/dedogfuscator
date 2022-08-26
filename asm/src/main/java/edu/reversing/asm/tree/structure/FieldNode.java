package edu.reversing.asm.tree.structure;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.function.Predicate;

public class FieldNode extends org.objectweb.asm.tree.FieldNode {

    private final String owner;
    private final Type type;

    public FieldNode(int api, int access, String owner, String name, String descriptor, String signature, Object value) {
        super(api, access, name, descriptor, signature, value);
        this.owner = owner;
        this.type = Type.getType(descriptor);
    }

    public FieldNode(int access, String owner, String name, String descriptor, String signature, Object value) {
        this(Opcodes.ASM9, access, owner, name, descriptor, signature, value);
    }

    public String key() {
        return owner + "." + name;
    }

    public String getOwner() {
        return owner;
    }

    public Type getType() {
        return type;
    }

    public boolean isType(Predicate<Type> predicate) {
        return predicate.test(type);
    }

    public boolean isType(String descriptor) {
        return isType(type -> type.getDescriptor().equals(descriptor));
    }
}
