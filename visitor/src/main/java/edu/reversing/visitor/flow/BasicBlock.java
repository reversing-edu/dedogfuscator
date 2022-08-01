package edu.reversing.visitor.flow;

import org.objectweb.asm.tree.MethodNode;

import java.util.LinkedList;
import java.util.List;

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

    @Override
    public String toString() {
        return super.toString();
    }
}
