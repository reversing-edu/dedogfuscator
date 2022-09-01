package edu.reversing.visitor.convention;

import com.google.inject.Inject;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class GenerifyVisitor extends Visitor {

    private int generified = 0;

    @Inject
    public GenerifyVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitClass(ClassNode cls) {
        if (cls.name.contains("/")) {
            return;
        }

        if (cls.interfaces.contains("java/lang/Iterable")) {
            System.out.println(cls.name);

            SignatureWriter signatureWriter = new SignatureWriter();
            signatureWriter.visitFormalTypeParameter("T");
            signatureWriter.visitClassType(cls.superName);
            signatureWriter.visitClassType("java/lang/Object");
            signatureWriter.visitClassType("java/lang/Iterable");
            signatureWriter.visitTypeVariable("T");
            System.out.println("-------------");
            //System.out.println(cls.name);
           // cls.signature = "<T:L" + cls.superName + ";>"
           // System.out.println(cls.interfaces);
            //System.out.println("NONE:" + cls.signature);

            generified++;
        }
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Converted ").append(generified).append(" classes into generics");
    }
}
