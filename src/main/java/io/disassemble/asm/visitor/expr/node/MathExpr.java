package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.AbstractInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class MathExpr extends BasicExpr {

    private BasicExpr expr1, expr2;

    private FieldExpr field;
    private ConstExpr constant;

    public MathExpr(AbstractInsnNode insn, int type) {
        super(insn, type);
    }

    public BasicExpr expr1() {
        return expr1;
    }

    public BasicExpr expr2() {
        return expr2;
    }

    private void setFieldIfMatch(BasicExpr expr) {
        if (expr.opcode() == GETFIELD || expr.opcode() == GETSTATIC ||
                expr.opcode() == PUTFIELD || expr.opcode() == PUTSTATIC) {
            field = (FieldExpr) expr;
        }
    }

    private void setConstIfMatch(BasicExpr expr) {
        if (expr.opcode() == LDC) {
            constant = (ConstExpr) expr;
        }
    }

    public void setExpressions(BasicExpr expr1, BasicExpr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        setFieldIfMatch(expr1);
        setFieldIfMatch(expr2);
        setConstIfMatch(expr1);
        setConstIfMatch(expr2);
    }

    public FieldExpr field() {
        return field;
    }

    public boolean hasField() {
        return field != null;
    }

    public ConstExpr constant() {
        return constant;
    }

    public boolean hasConstant() {
        return constant != null;
    }
}
