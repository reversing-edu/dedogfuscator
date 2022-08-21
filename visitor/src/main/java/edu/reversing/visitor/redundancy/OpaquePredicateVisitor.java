package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import java.util.*;

public class OpaquePredicateVisitor extends Visitor {

    private final List<InvokeExpr> verdicts = new LinkedList<>();

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

            verdicts.add(call);
        }
    }

    private class ConstantParameterVerifier extends ExprVisitor {

        @Override
        public void visitInvoke(InvokeExpr call) {
            System.out.println(verdicts.size());
            //TODO check overriden
            //also child calls like npc.getModel instead of actor.getModel
            Set<String> remove = new HashSet<>();
            for (InvokeExpr verdict : verdicts) {
                if (verdict.key().equals(call.key())) {
                    Expr last = getLastArg(call);
                    if (!(last instanceof NumberExpr)) {
                        continue;
                    }

                    //method was called without a constant, remove it
                    remove.add(verdict.key());
                }
            }

            verdicts.removeIf(x -> remove.contains(x.key()));
        }
    }

    private class BranchRemover extends ExprVisitor {

        @Override
        public void visitBinaryJump(BinaryJumpStmt stmt) {
            System.out.println(verdicts.size());
            process(stmt.getLeft());
        }

        @Override
        public void visitUnaryJump(UnaryJumpStmt stmt) {
            process(stmt.getLeft());
        }

        private void process(Expr expr) {
            //TODO check hierarchy methods
            MethodNode method = expr.getRoot().getMethod();
            if (verdicts.stream().noneMatch(call -> call.key().equals(method.key()))) {
                return;
            }

            System.out.println("Weed");

            if (!(expr instanceof VarExpr param)) {
                return;
            }
            int argCount = Type.getArgumentTypes(method.desc).length;
            int startIndex = (method.access & ACC_STATIC) > 0 ? 0 : 1;
            int varIndex = param.getIndex();
            int lastArgIndex = (startIndex + argCount) - 1;
            if (lastArgIndex == varIndex) {
                System.out.println(expr.getParent());
            }
        }
    }
}
