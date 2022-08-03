package edu.reversing.asm.tree;

import org.objectweb.asm.Opcodes;

public class FieldNode extends org.objectweb.asm.tree.FieldNode {

    public String owner;

    public FieldNode(int api, int access, String owner, String name, String descriptor, String signature, Object value) {
        super(api, access, name, descriptor, signature, value);
        this.owner = owner;
    }

    public FieldNode(int access, String owner, String name, String descriptor, String signature, Object value) {
        this(Opcodes.ASM9, access, owner, name, descriptor, signature, value);
    }
}
