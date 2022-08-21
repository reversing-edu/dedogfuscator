package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

//TODO this is slow and has a few other TODOs
//also doesn't seem to find all of them, misses some (seemingly static methods? but i didnt check)
public class OpaquePredicateVisitor extends Visitor {

    private final Set<String> verdicts = new HashSet<>();

    private int removed = 0;

    @Inject
    public OpaquePredicateVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        ExprTree tree = method.getExprTree(true);
        //find method calls which uses constant parameters
        tree.accept(new ConstantParameterFinder());

        //verify that the verdicts are ONLY called with constants
        tree.accept(new ConstantParameterVerifier());

        //find branches in those methods using those constants and remove them
        tree.accept(new BranchRemover());
    }

    private Expr getLastArg(InvokeExpr call) {
        List<Expr> args = call.getArgs();
        if (args.isEmpty()) {
            return null;
        }

        return args.get(args.size() - 1);
    }

    @Override
    public void postVisit() {
        System.out.println("Removed " + removed + " opaque predicates");
    }

    private class ConstantParameterFinder extends ExprVisitor {

        @Override
        public void visitInvoke(InvokeExpr call) {
            //has to be the last arg
            Expr last = getLastArg(call);
            if (!(last instanceof NumberExpr constant)) {
                return;
            }

            //check it's an integer constant
            AbstractInsnNode instruction = constant.getInstruction();
            if (instruction instanceof LdcInsnNode ldc
                    && (ldc.cst instanceof Float || ldc.cst instanceof Double)) {
                return;
            }

            //ensure owner in classpath
            String owner = call.getInstruction().owner;
            if (!context.getLibrary().loaded(owner)) {
                return;
            }

            verdicts.add(call.key());
        }
    }

    private class ConstantParameterVerifier extends ExprVisitor {

        @Override
        public void visitInvoke(InvokeExpr call) {
            //TODO check overriden
            //also child calls like npc.getModel instead of actor.getModel
            Set<String> remove = new HashSet<>();
            for (String verdict : verdicts) {
                if (verdict.equals(call.key())) {
                    Expr last = getLastArg(call);
                    if (last instanceof NumberExpr) {
                        continue;
                    }

                    //method was called without a constant, remove it
                    remove.add(verdict);
                }
            }

            verdicts.removeIf(remove::contains);
        }
    }

    private class BranchRemover extends ExprVisitor {

        @Override
        public void visitBinaryJump(BinaryJumpStmt stmt) {
            process(stmt.getLeft());
        }

        @Override
        public void visitUnaryJump(UnaryJumpStmt stmt) {
            process(stmt.getLeft());
        }

        private void process(Expr expr) {
            //TODO check hierarchy methods
            MethodNode method = expr.getRoot().getMethod();
            if (verdicts.stream().noneMatch(call -> call.equals(method.key()))) {
                return;
            }

            if (!(expr instanceof VarExpr param)) {
                return;
            }

            int argCount = Type.getArgumentTypes(method.desc).length;
            int startIndex = (method.access & ACC_STATIC) > 0 ? 0 : 1;
            int varIndex = param.getIndex();
            int lastArgIndex = (startIndex + argCount) - 1;
            if (lastArgIndex == varIndex) {
                JumpExpr stmt = (JumpExpr) expr.getParent();
                Expr next = stmt.getNext();
                if (next == null || (next.getOpcode() != ATHROW && next.getOpcode() != RETURN)) {
                    return;
                }

                //shouldn't need, but validate it anyway
                if (next.getOpcode() == ATHROW) {
                    InvokeExpr exception = (InvokeExpr) next.layer(INVOKESPECIAL);
                    if (exception == null || !exception.getInstruction().owner.contains("IllegalState")) {
                        return;
                    }
                }

                System.out.println(method.owner + "." + method.name + method.desc);
                System.out.println(stmt);
                System.out.println(next);
                System.out.println();

                //TODO this breaks ClassWriter COMPUTE_MAXS
                //redirect to fallthrough block
                /*JumpInsnNode go = new JumpInsnNode(GOTO, stmt.getInstruction().label);
                method.instructions.insert(stmt.getInstruction(), go);

                for (AbstractInsnNode terminator : next.collapse()) {
                    method.instructions.remove(terminator);
                }

                for (AbstractInsnNode condition : stmt.collapse()) {
                    method.instructions.remove(condition);
                }*/
                removed++;
            }
        }
    }
}
