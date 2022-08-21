package edu.reversing.visitor.strahler;

import com.google.inject.Inject;
import edu.reversing.asm.tree.flow.BasicBlock;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.*;

import java.util.*;

//Note: The better way to do this would be to build an SSA graph to represent the CFG and variable definitions better
public class StrahlerNumberVisitor extends Visitor {

    private int strahler = 0;
    private int inserted = 0;

    @Inject
    public StrahlerNumberVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        Map<Integer, LocalVariableDefinition> defs = new HashMap<>();
        if (cls.name.equals("a") && method.name.equals("iu")) {
            findReadWrites(method.getBasicBlocks(true), defs);
            //TODO compute scopes of var definition?
            //maybe just declare a new variable at each "write"
            //e.g
            //var1 = var2;
            //var1++;
            //field = var1;
            //var1 = var4; -> new var would be declared here instead of var1
/*
            for (LocalVariableDefinition def : defs.values()) {
                int index = def.getIndex();
                Queue<BasicBlock> queue = new LinkedList<>(def.getAccess());
                while (!queue.isEmpty()) {
                    BasicBlock current = queue.remove();
                    if (def.getWrites().contains(current)) {
                        index++;
                    }

                    for (AbstractInsnNode instruction : current.getInstructions()) {
                        if (instruction instanceof VarInsnNode local
                                && local.var == def.getIndex()) {
                            local.var = index;
                        }
                    }
                }
            }*/
        }
    }

    private void findReadWrites(List<BasicBlock> blocks, Map<Integer, LocalVariableDefinition> defs) {
        for (BasicBlock block : blocks) {
            for (AbstractInsnNode instruction : block.getInstructions()) {
                if (instruction instanceof VarInsnNode local) {
                    LocalVariableDefinition def = defs.computeIfAbsent(local.var, LocalVariableDefinition::new);
                    int opcode = local.getOpcode();
                    if (opcode >= ILOAD && opcode <= ALOAD) {
                        def.getReads().add(block);
                    } else {
                        def.getWrites().add(block);
                    }
                } else if (instruction instanceof IincInsnNode increment) {
                    LocalVariableDefinition def = defs.computeIfAbsent(increment.var, LocalVariableDefinition::new);
                    def.getWrites().add(block);
                }
            }
        }
    }
}
