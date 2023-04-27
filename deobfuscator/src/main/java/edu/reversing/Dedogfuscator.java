package edu.reversing;

import com.google.inject.*;
import edu.reversing.asm.tree.classpath.Library;
import edu.reversing.configuration.Configuration;
import edu.reversing.visitor.VisitorContext;
import edu.reversing.visitor.expr.multiplier.MultiplierIdentifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

public class Dedogfuscator {

  public static void main(String... args) throws IOException {
    Configuration configuration = Configuration.valueOf(args);

    Injector injector = Guice.createInjector(new Module());
    Library library = injector.getInstance(Library.class);
    library.load(configuration.getInput(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    VisitorContext context = injector.getInstance(VisitorContext.class);
    context.inject(injector);         //i dont like this
    context.transform();

    library.write(configuration.getOutput(), ClassWriter.COMPUTE_MAXS);
  }

  private static class Module extends AbstractModule {

    @Override
    protected void configure() {
      bind(Library.class).asEagerSingleton();
      bind(MultiplierIdentifier.class).asEagerSingleton();
    }
  }
}
