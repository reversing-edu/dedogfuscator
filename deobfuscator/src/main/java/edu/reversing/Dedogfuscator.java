package edu.reversing;

import edu.reversing.asm.Hierarchy;
import edu.reversing.asm.Library;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.flow.FlowVisitor;
import edu.reversing.visitor.redundancy.AccessVisitor;
import edu.reversing.visitor.redundancy.TryCatchVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Dedogfuscator {

    public static void main(String[] args) throws IOException {
        Library library = new Library();
        Path path = Paths.get(args[0]);
        library.load(path, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        VisitorContext context = new VisitorContext(
                library,
                new Hierarchy(library)
        );

        context.add(new AccessVisitor(context));
        context.add(new TryCatchVisitor(context));
        context.add(new FlowVisitor(context));
        context.transform();

        library.write(path.getParent().resolve("trans-" + path.getFileName()), ClassWriter.COMPUTE_MAXS);
    }
}
