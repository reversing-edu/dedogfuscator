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
        //find method calls which uses constant parameters
        method.getExprTree(false).accept(new ConstantParameterFinder());
      }
    }

    for (ClassNode cls : context.getLibrary()) {
      for (MethodNode method : cls.methods) {
        //verify that the verdicts are ONLY called with constants
        method.getExprTree(false).accept(new ConstantParameterVerifier());
      }
    }
  }

  @Override
  public void visitCode(ClassNode cls, MethodNode method) {
    //TODO check hierarchy methods
    if (verdicts.stream().noneMatch(call -> call.equals(method.key()))) {
      return;
    }

    int index = (method.access & ACC_STATIC) > 0 ? -1 : 0;
    for (Type arg : Type.getArgumentTypes(method.desc)) {
      switch (arg.getDescriptor()) {
        case "D", "J" -> index += 2;
        default -> index += 1;
      }
    }

    //find branches in verdict methods using those constants and insert a goto to redirect the flow
    method.getExprTree(true).accept(new GotoInserter(index));
  }

  private Expr getLastArg(InvokeExpr call) {
    List<Expr> args = call.getArgs();
    if (args.isEmpty()) {
      return null;
    }

    return args.get(args.size() - 1);
  }

  @Override
  public void output(StringBuilder output) {
    output.append("Removed ");
    output.append(removed);
    output.append(" opaque predicates");
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
      for (String verdict : verdicts) {
        if (verdict.equals(call.key())) {
          Expr last = getLastArg(call);
          if (last instanceof NumberExpr) {
            continue;
          }

          verdicts.remove(verdict);
          break;
        }
      }
    }
  }

  private class GotoInserter extends ExprVisitor {

    private final int lastParamIndex;

    private GotoInserter(int lastParamIndex) {
      this.lastParamIndex = lastParamIndex;
    }

    @Override
    public void visitBinaryJump(BinaryJumpStmt stmt) {
      process(stmt.getLeft());
    }

    @Override
    public void visitUnaryJump(UnaryJumpStmt stmt) {
      process(stmt.getLeft());
    }

    private void process(Expr expr) {
      if (!(expr instanceof VarExpr param) || param.getIndex() != lastParamIndex) {
        return;
      }

      JumpExpr stmt = (JumpExpr) expr.getParent();

      //TODO using getNext to verify this doesn't always work, but it shouldn't be needed. may need to fix if theres false positives being removed
   /*         Expr next = stmt.getNext();
            if (next == null || (next.getOpcode() != ATHROW && next.getOpcode() != RETURN)) {
                return;
            }*/

      //redirect to fallthrough block
      MethodNode method = expr.getRoot().getMethod();
      JumpInsnNode go = new JumpInsnNode(GOTO, stmt.getInstruction().label);
      method.instructions.insert(expr.getInstruction(), go);
      method.instructions.remove(expr.getInstruction());

      //should be removing the statement too but i got asm errors doing that?

      removed++;
    }
  }
}
