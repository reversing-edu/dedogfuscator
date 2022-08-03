package edu.reversing.visitor.flow;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ClassNode;
import edu.reversing.asm.tree.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.*;

public class FlowVisitor extends Visitor {

    private int generated = 0;
    private int gotos = 0;

    @Inject
    public FlowVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        if (method.tryCatchBlocks.size() > 0 || method.instructions.size() == 0) {
            return;
        }

        removeRedundantGotos(method);

        FlowAnalyzer analyzer = new FlowAnalyzer();
        try {
            analyzer.analyze(cls.name, method);
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }

        dfs(method, analyzer);
        generated += analyzer.getBlocks().size();

        //TODO dfs works to sort blocks but it leaves asm InsnList broken and with null insns
        //this could require reproducing labels or using AbstractInsnNode#clone instead of adding the insn directly (line 64)
    }

    public void dfs(MethodNode method, FlowAnalyzer analyzer) {
        List<BasicBlock> blocks = analyzer.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }

        InsnList instructions = new InsnList();
        Queue<BasicBlock> stack = new LinkedList<>();
        stack.add(blocks.get(0));
        Set<BasicBlock> visited = new HashSet<>();
        while (!stack.isEmpty()) {
            BasicBlock current = stack.remove();
            if (!visited.contains(current)) {
                visited.add(current);

                for (BasicBlock child : current.getChildren()) {
                    stack.add(child.getRoot());
                }

                if (current.getSuccessor() != null) {
                    stack.add(current.getSuccessor());
                }

                for (int i = current.getStart(); i < current.getEnd(); i++) {
                    AbstractInsnNode instruction = method.instructions.get(i);
                    instructions.add(instruction);
                }
            }
        }

        method.instructions = instructions;
    }

    private void removeRedundantGotos(MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() != GOTO) {
                continue;
            }

            JumpInsnNode jump = (JumpInsnNode) instruction;
            AbstractInsnNode next = jump.getNext();
            if (next == null || next.getType() != AbstractInsnNode.LABEL) {
                continue;
            }

            if (jump.label == next) {
                method.instructions.remove(instruction);
                gotos++;
            }
        }
    }

    @Override
    public void postVisit() {
        System.out.println("Generated " + generated + " basic blocks and removed " + gotos + " gotos");
    }
}
