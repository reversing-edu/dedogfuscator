package edu.reversing.asm.tree;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;

public class ClassNode extends org.objectweb.asm.tree.ClassNode {

    //we need to use our FieldNode and MethodNode instead of asms
    //i don't know if this is the best or correct way to do it
    //if you can think of any side effects or anything this would break please say

    public final List<FieldNode> fields = new ArrayList<>();
    public final List<MethodNode> methods = new ArrayList<>();

    public ClassNode() {
        super(Opcodes.ASM9);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldNode field = new FieldNode(access, this.name, name, descriptor, signature, value);
        fields.add(field);
        super.fields.add(field);
        return field;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodNode method = new MethodNode(access, this.name, name, descriptor, signature, exceptions);
        methods.add(method);
        super.methods.add(method);
        return method;
    }
}
