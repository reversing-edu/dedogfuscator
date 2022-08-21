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
    public void preVisit() {
        for (ClassNode cls : context.getLibrary()) {
            for (MethodNode method : cls.methods) {
                ExprTree tree = method.getExprTree(true);
                tree.accept(new ConstantParameterFinder()); //find method calls which uses constant parameters
                tree.accept(new ConstantParameterVerifier()); //verify that the verdicts are ONLY called with constants
            }
        }
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        ExprTree tree = method.getExprTree(true);

        //find branches in verdict methods using those constants and insert a goto to redirect the flow
        tree.accept(new GotoInserter());
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

    private class GotoInserter extends ExprVisitor {

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

            if (param.getIndex() != getLastParameterIndex(method)) {
                return;
            }

            JumpExpr stmt = (JumpExpr) expr.getParent();
            Expr next = stmt.getNext();
            if (next == null || (next.getOpcode() != ATHROW && next.getOpcode() != RETURN)) {
                //TODO using getNext doesn't always work
                return;
            }

            //redirect to fallthrough block
            JumpInsnNode go = new JumpInsnNode(GOTO, stmt.getInstruction().label);
            method.instructions.insert(expr.getInstruction(), go);
            method.instructions.remove(expr.getInstruction());

            //should be removing the statement too but i got asm errors doing that?

            removed++;
        }

        private int getLastParameterIndex(MethodNode method) {
            int index = (method.access & ACC_STATIC) > 0 ? -1 : 0;
            for (Type arg : Type.getArgumentTypes(method.desc)) {
                switch (arg.getDescriptor()) {
                    case "D", "J" -> index += 2;
                    default -> index += 1;
                }
            }
            return index;
        }
    }
}
