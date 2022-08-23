package edu.reversing.asm.tree.structure;

import edu.reversing.asm.analysis.ControlFlowAnalyzer;
import edu.reversing.asm.tree.flow.BasicBlock;
import edu.reversing.asm.tree.ir.ExprTree;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.Collections;
import java.util.List;

public class MethodNode extends org.objectweb.asm.tree.MethodNode {

    public String owner;

    private ExprTree tree;
    private List<BasicBlock> blocks;

    public MethodNode(int access, String owner, String name, String descriptor, String signature, String[] exceptions) {
        this(Opcodes.ASM9, access, owner, name, descriptor, signature, exceptions);
    }

    public MethodNode(int api, int access, String owner, String name, String descriptor, String signature, String[] exceptions) {
        super(api, access, name, descriptor, signature, exceptions);
        this.owner = owner;
    }

    public ExprTree getExprTree(boolean forceBuild) {
        if (forceBuild || tree == null) {
            tree = new ExprTree(this);
            tree.build();
        }
        return tree;
    }

    //Note: This method is raw blocks not dfs sorted
    public List<BasicBlock> getBasicBlocks(boolean forceBuild) {
        if (forceBuild || blocks == null) {
            ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
            try {
                analyzer.analyze(owner, this);
                blocks = analyzer.getBlocks();
            } catch (AnalyzerException e) {
                e.printStackTrace();
                blocks = Collections.emptyList();
            }
        }
        return blocks;
    }

    private BasicBlock getBasicBlock(AbstractInsnNode instruction) {
        int index = instructions.indexOf(instruction);
        for (BasicBlock block : blocks) {
            if (block.contains(index)) {
                return block;
            }
        }

        return null;
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
