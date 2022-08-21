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
            System.out.println(method.localVariables.size());
            findReadWrites(method.getBasicBlocks(true), defs);
            //TODO compute scopes of var definition?
            //maybe just declare a new variable at each "write"
            //e.g
            //var1 = var2;
            //var1++;
            //field = var1;
            //var1 = var4; -> new var would be declared here instead of var1
        }
    }

    private void findReadWrites(List<BasicBlock> blocks, Map<Integer, LocalVariableDefinition> scopes) {
        for (BasicBlock block : blocks) {
            for (AbstractInsnNode instruction : block.getInstructions()) {
                if (instruction instanceof VarInsnNode local) {
                    LocalVariableDefinition scope = scopes.computeIfAbsent(local.var, LocalVariableDefinition::new);
                    int opcode = local.getOpcode();
                    if (opcode >= ILOAD && opcode <= ALOAD) {
                        scope.getReads().add(block);
                    } else {
                        scope.getWrites().add(block);
                    }
                } else if (instruction instanceof IincInsnNode increment) {
                    LocalVariableDefinition scope = scopes.computeIfAbsent(increment.var, LocalVariableDefinition::new);
                    scope.getWrites().add(block);
                }
            }
        }
    }
}
