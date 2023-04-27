package edu.reversing.visitor.redundancy;

import com.google.inject.Inject;
import edu.reversing.asm.tree.classpath.Hierarchy;
import edu.reversing.asm.tree.classpath.Library;
import edu.reversing.asm.tree.ir.InvokeExpr;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;

import java.util.HashSet;
import java.util.Set;

public class UnusedMethodVisitor extends Visitor {

  private final Set<String> called = new HashSet<>();

  private int total = 0;
  private int removed = 0;

  @Inject
  public UnusedMethodVisitor(VisitorContext context) {
    super(context);
  }

  @Override
  public void visitClass(ClassNode cls) {
    total += cls.methods.size();
  }

  @Override
  public void visitCode(ClassNode cls, MethodNode method) {
    if (!cls.name.equals("client") || method.name.equals("init")) {
      return;
    }

    // //TODO this is super lenient but it does the job, a more accurate and better approach would be to
    ////traverse outgoing method calls starting from client.init()
    method.getExprTree(true).accept(new ExprVisitor() {
      @Override
      public void visitInvoke(InvokeExpr call) {
        if (!called.contains(call.key())) {
          called.add(call.key());
          call.accept(this);
        }
      }
    });
  }


  @Override
  public void postVisit() {
    Library library = context.getLibrary();
    Hierarchy hierarchy = context.getHierarchy();

    for (ClassNode cls : library) {
      Set<MethodNode> unused = new HashSet<>();
      for (MethodNode method : cls.methods) {
        if (!isMethodUsed(cls, method, library, hierarchy)) {
          MethodNode parentMethod = hierarchy.getParentMethod(cls, method);
          if (parentMethod == null) {
            unused.add(method);
          }
        }
      }

      cls.methods.removeAll(unused);
      removed += unused.size();
    }
  }

  @Override
  public void output(StringBuilder output) {
    output.append("Removed ");
    output.append(removed);
    output.append("/");
    output.append(total);
    output.append(" methods");
  }

  private boolean isMethodUsed(ClassNode cls, MethodNode method, Library library, Hierarchy hierarchy) {
    if (method.name.contains(">") || cls.name.contains("/")) { //skip <init>, <clinit> and libs
      return true;
    }

    String mName = cls.name + "." + method.name + method.desc;
    if (called.contains(mName)) {
      return true;
    }

    for (ClassNode parent : hierarchy.getParents(cls.name)) {
      if (!library.loaded(parent.name)) {
        return true;
      }
      if (called.contains(parent.name + "." + method.name + method.desc)) {
        return true;
      }
    }

    for (ClassNode child : hierarchy.getChildren(cls.name)) {
      if (called.contains(child.name + "." + method.name + method.desc)) {
        return true;
      }
    }

    return false;
  }
}
