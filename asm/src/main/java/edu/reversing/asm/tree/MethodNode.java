package edu.reversing.asm.tree;

import org.objectweb.asm.Opcodes;

public class MethodNode extends org.objectweb.asm.tree.MethodNode {

    public String owner;

    public MethodNode(int access, String owner, String name, String descriptor, String signature, String[] exceptions) {
        this(Opcodes.ASM9, access, owner, name, descriptor, signature, exceptions);
    }

    public MethodNode(int api, int access, String owner, String name, String descriptor, String signature, String[] exceptions) {
        super(api, access, name, descriptor, signature, exceptions);
        this.owner = owner;
    }
}
