package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.util.IndexedDeque;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class BasicExpr implements Iterable<BasicExpr> {

    /**
     * An indexed string representation of expression types.
     */
    public static final String[] LABELS = {
            "binary", "nary", "nullary", "quaternary", "ternary", "unary"
    };

    public static final int OP_BINARY = 0;
    public static final int OP_NARY = 1;
    public static final int OP_NULLARY = 2;
    public static final int OP_QUATERNARY = 3;
    public static final int OP_TERNARY = 4;
    public static final int OP_UNARY = 5;

    public final AbstractInsnNode insn;
    public final int type;

    private BasicExpr left, right, parent;

    private final IndexedDeque<BasicExpr> children = new IndexedDeque<>();

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param insn The instruction to use.
     * @param type The type of expression.
     */
    public BasicExpr(AbstractInsnNode insn, int type) {
        this.insn = insn;
        this.type = type;
    }

    @Override
    public Iterator<BasicExpr> iterator() {
        return children().iterator();
    }

    /**
     * Gets the amount of instructions that are expected to be followed by this instruction.
     *
     * @return The amount of instructions that are expected to be followed by this instruction.
     */
    public int proceeding() {
        switch (type) {
            case OP_UNARY: {
                return 1;
            }
            case OP_BINARY: {
                return 2;
            }
            case OP_TERNARY: {
                return 3;
            }
            case OP_QUATERNARY: {
                return 4;
            }
            case OP_NARY: {
                boolean loadVar = (opcode() == INVOKEVIRTUAL || opcode() == INVOKESPECIAL);
                if (loadVar || opcode() == INVOKEVIRTUAL || opcode() == INVOKEINTERFACE ||
                        opcode() == INVOKESPECIAL) {
                    String desc = ((MethodInsnNode) insn).desc;
                    int sizes = Type.getArgumentsAndReturnSizes(desc);
                    // argSize = (sizes >> 2)
                    // retSize = (sizes & 0x03)
                    // subtract 1 if loadVar since INVOKESTATIC/INVOKEDYNAMIC do not need to call ALOAD_0
                    return (sizes >> 2) - (loadVar ? 0 : 1);
                } else if (opcode() == MULTIANEWARRAY) {
                    return ((MultiANewArrayInsnNode) insn).dims;
                }
            }
            default: {
                return 0;
            }
        }
    }

    /**
     * Sets the expression preceding this expression.
     *
     * @param left The leftward expression to use.
     */
    public void setLeft(BasicExpr left) {
        this.left = left;
    }

    /**
     * Retrieves the expression preceding this expression.
     *
     * @return The expression preceding this expression.
     */
    public BasicExpr left() {
        return left;
    }

    /**
     * Sets the expression succeeding this expression.
     *
     * @param right The rightward expression to use.
     */
    public void setRight(BasicExpr right) {
        this.right = right;
    }

    /**
     * Retrieves the expression succeeding this expression.
     *
     * @return The expression succeeding this expression.
     */
    public BasicExpr right() {
        return right;
    }

    /**
     * Retrieves the expression that this expression belongs to.
     *
     * @return The expression that this expression belongs to.
     */
    public BasicExpr parent() {
        return parent;
    }

    /**
     * Adds a child to this expression.
     *
     * @param expr The expression to add.
     */
    public void addChild(BasicExpr expr) {
        expr.parent = this;
        children.addFirst(expr);
    }

    /**
     * Retrieves an IndexedDeque of expressions belonging to this expression.
     *
     * @return An IndexedDeque of expressions belonging to this expression.
     */
    public IndexedDeque<BasicExpr> children() {
        return children;
    }

    /**
     * Gets the opcode of this expression's instruction.
     *
     * @return The opcode of this expression's instruction.
     */
    public int opcode() {
        return insn.getOpcode();
    }

    /**
     * Returns this expression as its source counterpart.
     *
     * @return This expression as its source counterpart.
     */
    public String decompile() {
        throw new IllegalStateException("[" + Assembly.toString(insn) + "] could not be compiled.");
    }

    private void print(String indent) {
        System.out.println(indent + Assembly.toString(insn) + " (" + LABELS[type] + ")");
        for (BasicExpr expr : children) {
            expr.print(indent + "  ");
        }
    }

    /**
     * Pretty-prints this expression.
     */
    public void print() {
        print("");
    }

    /**
     * Constructs the respective BasicExpr for the given instruction and type.
     *
     * @param insn The instruction to resolve.
     * @param type The type of expression.
     * @return The respective BasicExpr for the given instruction and type.
     */
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
            case IADD:
            case LADD:
            case FADD:
            case DADD:
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
            case IREM:
            case LREM:
            case FREM:
            case DREM:
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR: {
                return new MathExpr(insn, type);
            }
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE: {
                return new CompBranchExpr(insn, type);
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IFNULL:
            case IFNONNULL: {
                return new BranchExpr(insn, type);
            }
            default: {
                return new BasicExpr(insn, type);
            }
        }
    }
}
