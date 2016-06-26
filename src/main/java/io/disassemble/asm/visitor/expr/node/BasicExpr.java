package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
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
public class BasicExpr<T extends AbstractInsnNode> implements Iterable<BasicExpr<AbstractInsnNode>> {

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

    public final ClassMethod method;
    public final int type;

    protected final T insn;

    private BasicExpr left, right, parent;

    final IndexedDeque<BasicExpr<AbstractInsnNode>> children = new IndexedDeque<>();

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param method The method this expression is in.
     * @param insn   The instruction to use.
     * @param type   The type of expression.
     */
    public BasicExpr(ClassMethod method, T insn, int type) {
        this.method = method;
        this.insn = insn;
        this.type = type;
    }

    /**
     * Retrieves the base instruction for this expression.
     *
     * @return The base instruction for this expression.
     */
    public T insn() {
        return insn;
    }

    @Override
    public Iterator<BasicExpr<AbstractInsnNode>> iterator() {
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
                // Some IincInsnNode's denote i++, ++i, --i, i-- instead of a for loop.
                // This is counteracted by checking if its #var is acting upon the method's parameter.
                if (opcode() == IINC) {
                    int var = ((IincInsnNode) insn).var;
                    Type[] types = method.parameterTypes();
                    // nonstatic methods include ALOAD_0, so this arg needs to be taken off in indexing.
                    int arg = ((method.access() & ACC_STATIC) == 0 ? (var - 1) : var);
                    if (arg < types.length) {
                        return 0;
                    }
                }
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
                if (opcode() == DUP) {
                    return (parent() != null && parent().opcode() == INVOKESPECIAL ? 1 : 0);
                } else {
                    boolean loadVar = (opcode() == INVOKEVIRTUAL || opcode() == INVOKESPECIAL ||
                            opcode() == INVOKEINTERFACE);
                    if (loadVar || opcode() == INVOKESTATIC || opcode() == INVOKEDYNAMIC) {
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
    public void addChild(BasicExpr<AbstractInsnNode> expr) {
        expr.parent = this;
        children.addFirst(expr);
    }

    /**
     * Retrieves an IndexedDeque of expressions belonging to this expression.
     *
     * @return An IndexedDeque of expressions belonging to this expression.
     */
    public IndexedDeque<BasicExpr<AbstractInsnNode>> children() {
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
        return "";
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
     * @param method The method the given instruction belongs to.
     * @param insn   The instruction to resolve.
     * @param type   The type of expression.
     * @return The respective BasicExpr for the given instruction and type.
     */
    public static BasicExpr resolve(ClassMethod method, AbstractInsnNode insn, int type) {
        switch (insn.getOpcode()) {
            case GETFIELD:
            case GETSTATIC:
            case PUTFIELD:
            case PUTSTATIC: {
                return new FieldExpr(method, (FieldInsnNode) insn, type);
            }
            case INVOKEINTERFACE:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEDYNAMIC: {
                return new MethodExpr(method, (MethodInsnNode) insn, type);
            }
            case LDC: {
                return new ConstExpr(method, (LdcInsnNode) insn, type);
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
                return new MathExpr(method, insn, type);
            }
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE: {
                return new CompBranchExpr(method, (JumpInsnNode) insn, type);
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IFNULL:
            case IFNONNULL: {
                return new BranchExpr(method, (JumpInsnNode) insn, type);
            }
            case ILOAD:
            case DLOAD:
            case FLOAD:
            case LLOAD:
            case ALOAD: {
                return new VarLoadExpr(method, (VarInsnNode) insn, type);
            }
            case ISTORE:
            case DSTORE:
            case FSTORE:
            case LSTORE:
            case ASTORE: {
                return new VarStoreExpr(method, (VarInsnNode) insn, type);
            }
            case RET: {
                return new VarExpr(method, (VarInsnNode) insn, type);
            }
            case BIPUSH:
            case SIPUSH:
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5: {
                return new PushExpr(method, insn, type);
            }
            default: {
                return new BasicExpr<>(method, insn, type);
            }
        }
    }

    /**
     * Retrieves the type of the given instruction.
     *
     * @param insn The instruction to get a type for.
     * @return The type of the given instruction.
     */
    public static int resolveType(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        switch (op) {
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
            case LXOR:
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
            case IASTORE:
            case BASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
            case CASTORE:
            case SASTORE:
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD: {
                return BasicExpr.OP_BINARY;
            }
            case DUP:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case MULTIANEWARRAY:
            case INVOKEDYNAMIC: {
                return BasicExpr.OP_NARY;
            }
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
            case SWAP:
            case ACONST_NULL:
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case LCONST_0:
            case LCONST_1:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case DCONST_0:
            case DCONST_1:
            case BIPUSH:
            case SIPUSH:
            case LDC:
            case JSR:
            case GETSTATIC:
            case NEW:
            case NEWARRAY:
            case ANEWARRAY:
            case CHECKCAST:
            case INSTANCEOF: {
                return BasicExpr.OP_NULLARY;
            }
            case DUP2_X2: {
                return BasicExpr.OP_QUATERNARY;
            }
            case DUP2_X1:
            case DUP_X2: {
                return BasicExpr.OP_TERNARY;
            }
            case DUP2:
            case DUP_X1:
            case I2L:
            case I2F:
            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2B:
            case I2C:
            case I2S:
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
            case IINC:
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case PUTSTATIC:
            case GETFIELD:
            case ARRAYLENGTH:
            case ATHROW:
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN: {
                return BasicExpr.OP_UNARY;
            }
            default: {
                return BasicExpr.OP_NULLARY;
            }
        }
    }
}
