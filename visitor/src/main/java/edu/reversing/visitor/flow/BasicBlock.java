package edu.reversing.visitor.flow;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicBlock {

    private final MethodNode method;
    private final List<BasicBlock> children;

    private BasicBlock predecessor;
    private BasicBlock successor;

    //I had issues using custom InsnList
    private int start;
    private int end;

    public BasicBlock(MethodNode method) {
        this.method = method;
        this.children = new LinkedList<>();
    }

    public MethodNode getMethod() {
        return method;
    }

    public List<BasicBlock> getChildren() {
        return children;
    }

    public BasicBlock getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(BasicBlock predecessor) {
        this.predecessor = predecessor;
    }

    public BasicBlock getSuccessor() {
        return successor;
    }

    public void setSuccessor(BasicBlock successor) {
        this.successor = successor;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean contains(int index) {
        return index >= start && index <= end;
    }

    public BasicBlock getRoot() {
        BasicBlock root = this;
        BasicBlock pred = predecessor;
        while (pred != null) {
            root = pred;
            pred = root.predecessor;
        }
        return root;
    }

    public List<AbstractInsnNode> getInstructions() {
        return IntStream.range(start, end).mapToObj(i -> method.instructions.get(i)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Block {");
        builder.append("\n");
        for (AbstractInsnNode instruction : getInstructions()) {
            if (instruction.getOpcode() == -1) {
                continue;
            }

            builder.append("\t");
            builder.append(Printer.OPCODES[instruction.getOpcode()]);
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }
}
