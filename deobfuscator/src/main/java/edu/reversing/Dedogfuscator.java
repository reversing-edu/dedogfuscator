package edu.reversing;

import edu.reversing.asm.Hierarchy;
import edu.reversing.asm.Library;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.stream.Collectors;

public class Dedogfuscator {

    public static void main(String[] args) {
        Library library = new Library();

        String name = "edu/reversing/visitor/Visitor";
        try {
            ClassNode cls = new ClassNode();
            ClassReader reader = new ClassReader(name);
            reader.accept(cls, ClassReader.SKIP_CODE);
            library.add(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Hierarchy hierarchy = new Hierarchy(library);
        VisitorContext context = new VisitorContext(library, hierarchy);

        System.out.println(hierarchy.getParents(name).stream().map(cls -> cls.name).collect(Collectors.toList()));
        System.out.println(hierarchy.getChildren(name).stream().map(cls -> cls.name).collect(Collectors.toList()));
    }
}
