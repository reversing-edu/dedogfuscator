package edu.reversing.visitor.flow;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

public class FlowAnalyzer extends Analyzer<BasicValue> {

    private final List<BasicBlock> blocks = new ArrayList<>();

    public FlowAnalyzer() {
        super(new BasicInterpreter());
    }

    @Override
    protected void init(String owner, MethodNode method) {
        BasicBlock block = new BasicBlock(method);
        blocks.add(block);

        for (int i = 0; i < method.instructions.size(); i++) {
            block.setEnd(block.getEnd() + 1);
            AbstractInsnNode instruction = method.instructions.get(i);
            if (instruction.getNext() != null && terminates(instruction.getType())) {
                block = new BasicBlock(method);
                block.setStart(i + 1);
                block.setEnd(i + 1);
                blocks.add(block);
            }
        }
    }

    private boolean terminates(int type) {
        return type == LABEL || type == JUMP_INSN || type == TABLESWITCH_INSN || type == LOOKUPSWITCH_INSN;
    }

    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        BasicBlock current = blocks.stream().filter(x -> x.contains(insnIndex)).findFirst().orElse(null);
        BasicBlock successor = blocks.stream().filter(x -> x.contains(successorIndex)).findFirst().orElse(null);
        if (current == successor
                || current == null
                || successor == null) {
            return;
        }

        if (successorIndex == insnIndex + 1) {
            current.setSuccessor(successor);
            successor.setPredecessor(current);
            return;
        }

        current.getChildren().add(current);
    }
}
