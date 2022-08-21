package edu.reversing.visitor.expr;

import com.google.inject.Inject;
import edu.reversing.asm.tree.ir.*;
import edu.reversing.asm.tree.ir.visitor.ExprVisitor;
import edu.reversing.asm.tree.structure.ClassNode;
import edu.reversing.asm.tree.structure.MethodNode;
import edu.reversing.visitor.Visitor;
import edu.reversing.visitor.VisitorContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class ExprOrderVisitor extends Visitor {

    private int ordered = 0;

    @Inject
    public ExprOrderVisitor(VisitorContext context) {
        super(context);
    }

    @Override
    public void visitCode(ClassNode cls, MethodNode method) {
        method.getExprTree(true).accept(new ExprVisitor() {
            @Override
            public void visitBinaryJump(BinaryJumpStmt stmt) {
                process(stmt, stmt.getLeft(), stmt.getRight());
            }

            @Override
            public void visitOperation(ArithmeticExpr operation) {
                process(operation, operation.getLeft(), operation.getRight());
            }
        });
    }

    private void process(Expr parent, Expr lhs, Expr rhs) {
        if (!isCommutative(parent) || !swap(lhs, rhs)) {
            return;
        }

        MethodNode method = parent.getRoot().getMethod();
        AbstractInsnNode[] left = lhs.collapse();
        AbstractInsnNode[] right = rhs.collapse();

        InsnList insertion = new InsnList();
        for (AbstractInsnNode instruction : right) {
            method.instructions.remove(instruction);
            insertion.add(instruction);
        }

        method.instructions.insertBefore(left[0], insertion);

        //TODO keep track of ordered count by types e.g. orderedConstants, orderedVarToField, orderedByExprSize
        ordered++;
    }

    private boolean isCommutative(Expr expr) {
        int opcode = expr.getOpcode();
        return (opcode >= IADD && opcode <= DADD)
                || (opcode >= IMUL && opcode <= DMUL)
                || opcode == IF_ACMPNE
                || opcode == IF_ICMPNE
                || opcode == IF_ACMPEQ
                || opcode == IF_ICMPEQ;
    }

    private boolean swap(Expr lhs, Expr rhs) {
        if (lhs instanceof ArrayLengthExpr) {
            //comparing 2 arraylengths, order by expr size
            if (rhs instanceof ArrayLengthExpr) {
                return lhs.getCumulativeSize() > rhs.getCumulativeSize();
            }

            //Always want array.length on right hand side
            return true;
        }

        //2 vars, order by var index
        if (lhs instanceof VarExpr v1 && rhs instanceof VarExpr v2) {
            return v1.getIndex() > v2.getIndex();
        }

        //Constant numbers should always be on right
        if (lhs instanceof NumberExpr) {
            return true;
        }

        //Field on left, Var on right
        if (lhs instanceof VarExpr && rhs instanceof FieldExpr) {
            return true;
        }

        if (lhs instanceof FieldExpr && rhs instanceof FieldExpr) {
            return swapField((FieldExpr) lhs, (FieldExpr) rhs);
        }

        //null on right side of comparison
        if (lhs.getOpcode() == ACONST_NULL) {
            return true;
        }

        return false;
    }

    private boolean swapField(FieldExpr lhs, FieldExpr rhs) {
        //Both fields are non static
        if (!lhs.isStatic() && !rhs.isStatic()) {
            //Caller contexts of the field (i.e if it was var.field then the context is the var)
            Expr lctx = lhs.get(0);
            Expr rctx = rhs.get(0);

            //If field callers are both local variables, prefer lower var index on left side
            if (lctx instanceof VarExpr v1 && rctx instanceof VarExpr v2) {
                return v1.getIndex() > v2.getIndex();
            }

            if (rctx.getOpcode() == DUP) {
                return true;
            }
        }

        //Prefer static ref on left side when opposite operand is nonstatic
        if (!lhs.isStatic() && rhs.isStatic()) {
            return true;
        }

        //Last case: Sort by expression size? i.e var.field is preferred on left over var.field.field.field
        return lhs.getCumulativeSize() > rhs.getCumulativeSize();
    }

    @Override
    public void output(StringBuilder output) {
        output.append("Ordered ");
        output.append(ordered);
        output.append(" expressions");
    }
}
