package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassMethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * A ClassMethodVisitor that replicates Basic/SourceInterpreter.
 */
public class InterpretingVisitor extends ClassMethodVisitor {

    @Override
    protected AbstractInsnNode current() {
        AbstractInsnNode current = super.current();
        int op = current.getOpcode();
        switch (op) {
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
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
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD:
            case DUP_X1:
            case DUP2: {
                visitBinaryOperation(current);
                break;
            }
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case MULTIANEWARRAY:
            case INVOKEDYNAMIC: {
                visitNaryOperation(current);
                break;
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
            case INSTANCEOF:
            case DUP: {
                visitNullaryOperation(current);
                break;
            }
            case DUP2_X2: {
                visitQuaternaryOperation(current);
                break;
            }
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
            case DUP2_X1:
            case DUP_X2: {
                visitTernaryOperation(current);
                break;
            }
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
                visitUnaryOperation(current);
                break;
            }
            default: {
                visitNullaryOperation(current); // assumed GOTO/LineNumberNode.
                break;
            }
        }
        return current;
    }

    /**
     * Visits instructions that pop 2 instructions from the stack.
     *
     * @param insn The base instruction causing a stack change.
     */
    public void visitBinaryOperation(AbstractInsnNode insn) {
    }

    /**
     * Visits instructions that pop N instructions from the stack.
     *
     * @param insn The base instruction causing a stack change.
     */
    public void visitNaryOperation(AbstractInsnNode insn) {
    }

    /**
     * Visits instructions that do not pop any instructions from the stack.
     *
     * @param insn The base instruction.
     */
    public void visitNullaryOperation(AbstractInsnNode insn) {
    }

    /**
     * Visits instructions that pop 4 instructions from the stack.
     *
     * @param insn The base instruction causing a stack change.
     */
    public void visitQuaternaryOperation(AbstractInsnNode insn) {
    }

    /**
     * Visits instructions that pop 3 instructions from the stack.
     *
     * @param insn The base instruction causing a stack change.
     */
    public void visitTernaryOperation(AbstractInsnNode insn) {
    }

    /**
     * Visits instructions that pop 1 instruction from the stack.
     *
     * @param insn The base instruction causing a stack change.
     */
    public void visitUnaryOperation(AbstractInsnNode insn) {
    }
}
