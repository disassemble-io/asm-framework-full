package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 *
 * A BasicExpr that represents an equation.
 */
public class MathExpr extends BasicExpr<AbstractInsnNode> {

    public MathExpr(ClassMethod method, AbstractInsnNode insn, int type) {
        super(method, insn, type);
    }

    /**
     * Retrieves the first expression in the equation.
     *
     * @return The first expression in the equation.
     */
    public BasicExpr expr1() {
        return children().get(0);
    }

    /**
     * Retrieves the second expression in the equation.
     *
     * @return The second expression in the equation.
     */
    public BasicExpr expr2() {
        try {
            return children().get(1);
        } catch (Exception e) {
            System.out.println("IOOBE @ " + Assembly.toString(insn()));
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Retrieves the field in this equation, if it exists.
     *
     * @return An Optional of the field in this equation, if it exists.
     */
    public Optional<FieldExpr> field() {
        BasicExpr expr1 = expr1(), expr2 = expr2();
        if (expr1 instanceof FieldExpr) {
            return Optional.of((FieldExpr) expr1);
        } else if (expr2 instanceof FieldExpr) {
            return Optional.of((FieldExpr) expr2);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the constant in this equation, if it exists.
     *
     * @return An Optional of the constant in this equation, if it exists.
     */
    public Optional<ConstExpr> constant() {
        BasicExpr expr1 = expr1(), expr2 = expr2();
        if (expr1 instanceof ConstExpr) {
            return Optional.of((ConstExpr) expr1);
        } else if (expr2 instanceof ConstExpr) {
            return Optional.of((ConstExpr) expr2);
        } else {
            return Optional.empty();
        }
    }
}
