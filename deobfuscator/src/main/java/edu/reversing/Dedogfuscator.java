package edu.reversing;

import edu.reversing.asm.Hierarchy;
import edu.reversing.asm.Library;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.ClassReader;

import java.nio.file.Paths;

public class Dedogfuscator {

    public static void main(String[] args) {
        Library library = Library.from(Paths.get(args[0]), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        Hierarchy hierarchy = new Hierarchy(library);

        VisitorContext context = new VisitorContext(library, hierarchy);
        context.transform();
    }
}
