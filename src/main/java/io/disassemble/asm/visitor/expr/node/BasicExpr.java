package io.disassemble.asm.visitor.expr.node;

import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class BasicExpr {

    public static final String[] LABELS = {"binary", "copy", "nary", "new", "return", "ternary", "unary"};

    public static final int OP_BINARY = 0;
    public static final int OP_COPY = 1;
    public static final int OP_NARY = 2;
    public static final int OP_NEW = 3;
    public static final int OP_RETURN = 4;
    public static final int OP_TERNARY = 5;
    public static final int OP_UNARY = 6;

    public final AbstractInsnNode insn;
    public final int type;

    private BasicExpr left, right;

    public BasicExpr(AbstractInsnNode insn, int type) {
        this.insn = insn;
        this.type = type;
    }

    public int proceeding() {
        switch (type) {
            case OP_UNARY: {
                return 1;
            }
            case OP_BINARY:
            case OP_RETURN: {
                return 2;
            }
            case OP_TERNARY: {
                return 3;
            }
            case OP_NARY: {
                boolean nSlot = (opcode() == INVOKEDYNAMIC || opcode() == INVOKESTATIC);
                if (nSlot || opcode() == INVOKEVIRTUAL || opcode() == INVOKEINTERFACE) {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    int sizes = Type.getArgumentsAndReturnSizes(min.desc);
                    return (sizes >> 2) - (nSlot ? 1 : 0);
                } else if (opcode() == MULTIANEWARRAY) {
                    return ((MultiANewArrayInsnNode) insn).dims;
                }
            }
            default: {
                return 0;
            }
        }
    }

    public void setLeft(BasicExpr left) {
        this.left = left;
    }

    public BasicExpr left() {
        return left;
    }

    public void setRight(BasicExpr right) {
        this.right = right;
    }

    public BasicExpr right() {
        return right;
    }

    public int opcode() {
        return insn.getOpcode();
    }

    public static BasicExpr resolve(AbstractInsnNode insn, int type) {
        switch (insn.getOpcode()) {
            case GETFIELD:
            case GETSTATIC:
            case PUTFIELD:
            case PUTSTATIC: {
                return new FieldExpr((FieldInsnNode) insn, type);
            }
            case LDC: {
                return new ConstExpr((LdcInsnNode) insn, type);
            }
            case IMUL:
            case IADD:
            case ISUB: {
                return new MathExpr(insn, type);
            }
            default: {
                return new BasicExpr(insn, type);
            }
        }
    }

    public static void setExtras(BasicExpr expr) {
        if (expr instanceof MathExpr) {
            ((MathExpr) expr).setExpressions(expr.left().left(), expr.left());
        }
    }
}
