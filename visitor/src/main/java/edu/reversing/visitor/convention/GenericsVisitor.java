package edu.reversing.visitor.convention;

import com.google.inject.Inject;
import edu.reversing.asm.tree.classpath.Hierarchy;
import edu.reversing.asm.tree.classpath.Library;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.*;
import edu.reversing.commons.Multimap;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Set;

public class GenericsVisitor extends Visitor {

  private final Multimap<String, ClassNode> genericTypeSubClasses = new Multimap<>();
  private final Set<String> genericTypeFields = new HashSet<>();

  @Inject
  public GenericsVisitor(VisitorContext context) {
    super(context);
  }

  @Override
  public void preVisit() {
    Library library = context.getLibrary();
    Hierarchy hierarchy = context.getHierarchy();

    //get all fields that are JDK type and have generic signatures (list, map etc)
    for (ClassNode cls : context.getLibrary()) {
      if (cls.signature != null || cls.name.contains("/")) {
        continue;
      }

      for (FieldNode field : cls.fields) {
        if (field.signature != null) {
          continue;
        }

        Type type = field.getType();
        if (isDisregarded(type.getDescriptor())) {
          continue;
        }

        String typeName = type.getClassName();
        if (library.loaded(typeName)) { //we only want to map jdk classes here
          continue;
        }

        ClassNode typeDef = defineClass(typeName);
        if (typeDef != null && typeDef.signature != null && typeDef.signature.contains(":")) {
          genericTypeFields.add(field.key());
        }
      }
    }

    for (ClassNode cls : library) {
      //now scan parents to see if the class is generifiable by a jdk supertype. e.g: extends Iterable, List, comparator
      //we need these parents so we can generify the class, and also check for overriden methods
      //and change their descs if needed. e.g compareTo(Object) -> compareTo(T)
      if (cls.name.contains("/") || cls.signature != null) {
        continue;
      }

      if (cls.superName.equals("java/lang/Object") && cls.interfaces.isEmpty()) {
        continue;
      }

      for (ClassNode parent : hierarchy.getParents(cls.name)) {
        if (parent.signature == null || !parent.signature.contains(":")) {
          continue;
        }

        genericTypeSubClasses.put(cls.name, parent);
      }
    }
  }

  private ClassNode defineClass(String name) {
    try {
      ClassNode cls = new ClassNode();
      cls.external = true;
      ClassReader reader = new ClassReader(name);
      reader.accept(cls, ClassReader.SKIP_CODE);
      return cls;
    } catch (Exception e) {
      return null;
    }
  }

  private boolean isDisregarded(String type) { //disregard  array casts and primitive casts?
    return type.startsWith("[") || type.length() == 1;
  }

  @Override
  public void visitCode(ClassNode cls, MethodNode method) {
    method.getExprTree(true).accept(new ExprVisitor() {
      @Override
      public void visitCast(CastExpr cast) {
        if (cast.getOpcode() != CHECKCAST) {
          return;
        }

        String type = cast.getType();
        if (isDisregarded(type) || type.contains("/")) {
          return;
        }

        Expr context = cast.getParent();
        if (context instanceof InvokeExpr call) {
          //cast is a method param
        }

        //TODO other casts? like local vars and fields?
      }
    });
  }

  @Override
  public void output(StringBuilder output) {
    output.append(genericTypeSubClasses.keySet());
    output.append("\n");
    output.append(genericTypeFields);
  }
}
