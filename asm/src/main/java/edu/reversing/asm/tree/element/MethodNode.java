package edu.reversing.asm.tree.element;

import edu.reversing.asm.tree.ir.ExprTree;
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

    public ExprTree getExprTree(boolean forceBuild) {
        ExprTree tree = new ExprTree(this);
        tree.build(forceBuild);
        return tree;
    }

    //TODO maybe some key cache/hashing
    public String key() {
        return owner + "." + name + desc;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MethodNode other && other.key().equals(key());
    }
}
