package edu.reversing;

import com.google.inject.*;
import edu.reversing.asm.Library;
import edu.reversing.configuration.Configuration;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.flow.FlowVisitor;
import edu.reversing.visitor.redundancy.AccessVisitor;
import edu.reversing.visitor.redundancy.TryCatchVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.nio.file.Paths;

public class Dedogfuscator {

    public static void main(String... args) throws IOException {
        Configuration configuration = Configuration.valueOf(args);

        Injector injector = Guice.createInjector(new Module());
        Library library = injector.getInstance(Library.class);
        library.load(configuration.getInput(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        VisitorContext context = injector.getInstance(VisitorContext.class);

        //i dont like this
        context.addFirst(injector.getInstance(FlowVisitor.class));
        context.addFirst(injector.getInstance(TryCatchVisitor.class));
        context.addFirst(injector.getInstance(AccessVisitor.class));
        context.transform();

        library.write(configuration.getOutput(), ClassWriter.COMPUTE_MAXS);
    }

    private static class Module extends AbstractModule {

        @Override
        protected void configure() {
            //i dont know if this is the correct way to do it
            bind(Library.class).asEagerSingleton();
        }
    }
}
