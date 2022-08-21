package edu.reversing.visitor.flow;

import com.google.inject.Inject;
import edu.reversing.asm.tree.flow.BasicBlock;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.*;

import java.util.*;

public class ControlFlowDFSVisitor extends Visitor {

    private int generated = 0;

    @Inject
    public ControlFlowDFSVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        if (method.tryCatchBlocks.size() > 0 || method.instructions.size() == 0) {
            return;
        }

        List<BasicBlock> blocks = method.getBasicBlocks(true);
        dfs(method, blocks);
        generated += blocks.size();
    }

    private void dfs(MethodNode method, List<BasicBlock> blocks) {
        if (blocks.isEmpty()) {
            return;
        }

        Map<LabelNode, LabelNode> otf = new HashMap<>() {
            @Override
            public LabelNode get(Object key) {
                LabelNode result = super.get(key);
                if (result == null) {
                    result = new LabelNode();
                    super.put((LabelNode) key, result);
                }
                return result;
            }
        };

        InsnList instructions = new InsnList();
        Deque<BasicBlock> stack = new LinkedList<>();
        stack.add(blocks.get(0));
        Set<BasicBlock> visited = new HashSet<>();
        while (!stack.isEmpty()) {
            BasicBlock current = stack.remove();
            if (!visited.contains(current)) {
                visited.add(current);

                for (BasicBlock child : current.getChildren()) {
                    stack.addFirst(child.getRoot());
                }

                if (current.getSuccessor() != null) {
                    stack.addFirst(current.getSuccessor());
                }

                for (int i = current.getStart(); i < current.getEnd(); i++) {
                    AbstractInsnNode instruction = method.instructions.get(i);
                    instructions.add(instruction.clone(otf));
                }
            }
        }

        method.instructions = instructions;
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Built CFGs with ");
        output.append(generated);
        output.append(" basic blocks and performed DFS on methods");
    }
}
