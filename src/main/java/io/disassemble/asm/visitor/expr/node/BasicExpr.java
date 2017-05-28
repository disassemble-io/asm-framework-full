package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.util.IndexedDeque;
import io.disassemble.asm.visitor.expr.ExprTreeVisitor;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class BasicExpr implements Iterable<BasicExpr> {

    public final ClassMethod method;
    public final int index, size;

    protected final AbstractInsnNode insn;

    private BasicExpr left, right, parent;

    final IndexedDeque<BasicExpr> children = new IndexedDeque<>();

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param method The method this expression is in.
     * @param insn   The instruction to use.
     * @param index  The index of this instruction in the reverse stack.
     * @param size   The amount of slots taken up by this instruction.
     */
    public BasicExpr(ClassMethod method, AbstractInsnNode insn, int index, int size) {
        this.method = method;
        this.insn = insn;
        this.index = index;
        this.size = size;
    }

    /**
     * Retrieves the base instruction for this expression.
     *
     * @return The base instruction for this expression.
     */
    public AbstractInsnNode insn() {
        return insn;
    }

    @Override
    public Iterator<BasicExpr> iterator() {
        return children().iterator();
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
     * Checks if the given child is within this expression's tree.
     *
     * @param expr The child to check existence for.
     * @return <tt>true</tt> if the child exists, otherwise <tt>false</tt>.
     */
    public boolean hasChildInTree(BasicExpr expr) {
        for (BasicExpr child : children()) {
            if (child.equals(expr) || child.hasChildInTree(expr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visits every child within this expression's tree.
     *
     * @param visitor The visitor to apply.
     */
    public void visitTree(ExprTreeVisitor visitor) {
        children().forEach(child -> {
            visitor.visitExpr(child);
            child.visitTree(visitor);
        });
    }

    /**
     * Returns this expression as its source counterpart.
     *
     * @return This expression as its source counterpart.
     */
    public String decompile() {
        return "";
    }

    /**
     * Pretty-prints this expression, starting with the given indent.
     */
    public void printWithIndent(String indent) {
        System.out.println(indent + Assembly.toString(insn) + " (" + size + ")");
        for (BasicExpr expr : children) {
            expr.printWithIndent(indent + "  ");
        }
    }

    /**
     * Pretty-prints this expression.
     */
    public void print() {
        printWithIndent("");
    }

    /**
     * Constructs the respective BasicExpr for the given instruction and type.
     *
     * @param method The method the given instruction belongs to.
     * @param insn   The instruction to resolve.
     * @param index  The index of this expression in the reverse stack.
     * @param size   The amount of slots taken up by this instruction.
     * @return The respective BasicExpr for the given instruction and type.
     */
    public static BasicExpr resolve(ClassMethod method, AbstractInsnNode insn, int index, int size) {
        switch (insn.getOpcode()) {
            case GETFIELD:
            case GETSTATIC:
            case PUTFIELD:
            case PUTSTATIC: {
                // System.out.println("    FieldExpr");
                return new FieldExpr(method, (FieldInsnNode) insn, index, size);
            }
            case INVOKEINTERFACE:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEDYNAMIC: {
                // System.out.println("    MethodExpr");
                return new MethodExpr(method, (MethodInsnNode) insn, index, size);
            }
            case LDC: {
                // System.out.println("    ConstExpr");
                return new ConstExpr(method, (LdcInsnNode) insn, index, size);
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
                // System.out.println("    MathExpr");
                return new MathExpr(method, insn, index, size);
            }
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE: {
                // System.out.println("    CompBranchExpr");
                return new CompBranchExpr(method, (JumpInsnNode) insn, index, size);
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IFNULL:
            case IFNONNULL: {
                // System.out.println("    BranchExpr");
                return new BranchExpr(method, (JumpInsnNode) insn, index, size);
            }
            case ILOAD:
            case DLOAD:
            case FLOAD:
            case LLOAD:
            case ALOAD: {
                // System.out.println("    VarLoadExpr");
                return new VarLoadExpr(method, (VarInsnNode) insn, index, size);
            }
            case ISTORE:
            case DSTORE:
            case FSTORE:
            case LSTORE:
            case ASTORE: {
                // System.out.println("    VarStoreExpr");
                return new VarStoreExpr(method, (VarInsnNode) insn, index, size);
            }
            case RET: {
                // System.out.println("    VarExpr");
                return new VarExpr(method, (VarInsnNode) insn, index, size);
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
                // System.out.println("    PushExpr");
                return new PushExpr(method, insn, index, size);
            }
            default: {
                // System.out.println("    BasicExpr");
                return new BasicExpr(method, insn, index, size);
            }
        }
    }
}
