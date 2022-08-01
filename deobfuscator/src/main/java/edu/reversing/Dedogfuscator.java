package edu.reversing;

import com.google.inject.*;
import edu.reversing.asm.Library;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.flow.FlowVisitor;
import edu.reversing.visitor.redundancy.AccessVisitor;
import edu.reversing.visitor.redundancy.TryCatchVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.nio.file.Paths;

public class Dedogfuscator {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new Module());
        Library library = injector.getInstance(Library.class);
        library.load(Paths.get(args[0]), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        VisitorContext context = injector.getInstance(VisitorContext.class);
        context.add(injector.getInstance(AccessVisitor.class));
        context.add(injector.getInstance(TryCatchVisitor.class));
        context.add(injector.getInstance(FlowVisitor.class));
        context.transform();

        library.write(Paths.get(args[1]), ClassWriter.COMPUTE_MAXS);
    }

    private static class Module extends AbstractModule {

        @Override
        protected void configure() {
            //i dont know if this is the correct way to do it
            bind(Library.class).asEagerSingleton();
        }
    }
}
