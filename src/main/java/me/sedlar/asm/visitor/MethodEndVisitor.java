package me.sedlar.asm.visitor;

import me.sedlar.asm.ClassMethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 1/13/2016
 */
public abstract class MethodEndVisitor extends ClassMethodVisitor {

    protected static Object THIS = new Object();

    protected static Object OTHER = new Object();
    private final List<InsnNode> exits = new ArrayList<>();
    protected boolean constructor;
    protected boolean superInitialized;
    protected List<Object> stackFrame;
    protected Map<Label, List<Object>> branches;

    @Override
    public void visitCode() {
        if (constructor) {
            stackFrame = new ArrayList<>();
            branches = new HashMap<>();
        } else {
            superInitialized = true;
            onMethodEnter();
        }
    }

    @Override
    public void visitLabel(LabelNode labelNode) {
        if (constructor && branches != null) {
            Label label = labelNode.getLabel();
            List<Object> frame = branches.get(label);
            if (frame != null) {
                stackFrame = frame;
                branches.remove(label);
            }
        }
    }

    @Override
    public void visitInsn(InsnNode insn) {
        int opcode = insn.getOpcode();
        if (constructor) {
            int size;
            switch (opcode) {
                case RETURN: { // empty stack
                    onMethodExit(insn);
                    break;
                }
                case IRETURN: // 1 before n/a after
                case FRETURN: // 1 before n/a after
                case ARETURN: // 1 before n/a after
                case ATHROW: { // 1 before n/a after
                    popValue();
                    onMethodExit(insn);
                    break;
                }
                case LRETURN: // 2 before n/a after
                case DRETURN: { // 2 before n/a after
                    popValue();
                    popValue();
                    onMethodExit(insn);
                    break;
                }
                case NOP:
                case LALOAD: // remove 2 add 2
                case DALOAD: // remove 2 add 2
                case LNEG:
                case DNEG:
                case FNEG:
                case INEG:
                case L2D:
                case D2L:
                case F2I:
                case I2B:
                case I2C:
                case I2S:
                case I2F:
                case ARRAYLENGTH: {
                    break;
                }
                case ACONST_NULL:
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                case F2L: // 1 before 2 after
                case F2D:
                case I2L:
                case I2D: {
                    pushValue(OTHER);
                    break;
                }
                case LCONST_0:
                case LCONST_1:
                case DCONST_0:
                case DCONST_1: {
                    pushValue(OTHER);
                    pushValue(OTHER);
                    break;
                }
                case IALOAD: // remove 2 add 1
                case FALOAD: // remove 2 add 1
                case AALOAD: // remove 2 add 1
                case BALOAD: // remove 2 add 1
                case CALOAD: // remove 2 add 1
                case SALOAD: // remove 2 add 1
                case POP:
                case IADD:
                case FADD:
                case ISUB:
                case LSHL: // 3 before 2 after
                case LSHR: // 3 before 2 after
                case LUSHR: // 3 before 2 after
                case L2I: // 2 before 1 after
                case L2F: // 2 before 1 after
                case D2I: // 2 before 1 after
                case D2F: // 2 before 1 after
                case FSUB:
                case FMUL:
                case FDIV:
                case FREM:
                case FCMPL: // 2 before 1 after
                case FCMPG: // 2 before 1 after
                case IMUL:
                case IDIV:
                case IREM:
                case ISHL:
                case ISHR:
                case IUSHR:
                case IAND:
                case IOR:
                case IXOR:
                case MONITORENTER:
                case MONITOREXIT: {
                    popValue();
                    break;
                }
                case POP2:
                case LSUB:
                case LMUL:
                case LDIV:
                case LREM:
                case LADD:
                case LAND:
                case LOR:
                case LXOR:
                case DADD:
                case DMUL:
                case DSUB:
                case DDIV:
                case DREM: {
                    popValue();
                    popValue();
                    break;
                }
                case IASTORE:
                case FASTORE:
                case AASTORE:
                case BASTORE:
                case CASTORE:
                case SASTORE:
                case LCMP: // 4 before 1 after
                case DCMPL:
                case DCMPG: {
                    popValue();
                    popValue();
                    popValue();
                    break;
                }
                case LASTORE:
                case DASTORE: {
                    popValue();
                    popValue();
                    popValue();
                    popValue();
                    break;
                }
                case DUP: {
                    pushValue(peekValue());
                    break;
                }
                case DUP_X1: {
                    size = stackFrame.size();
                    stackFrame.add(size - 2, stackFrame.get(size - 1));
                    break;
                }
                case DUP_X2: {
                    size = stackFrame.size();
                    stackFrame.add(size - 3, stackFrame.get(size - 1));
                    break;
                }
                case DUP2: {
                    size = stackFrame.size();
                    stackFrame.add(size - 2, stackFrame.get(size - 1));
                    stackFrame.add(size - 2, stackFrame.get(size - 1));
                    break;
                }
                case DUP2_X1: {
                    size = stackFrame.size();
                    stackFrame.add(size - 3, stackFrame.get(size - 1));
                    stackFrame.add(size - 3, stackFrame.get(size - 1));
                    break;
                }
                case DUP2_X2: {
                    size = stackFrame.size();
                    stackFrame.add(size - 4, stackFrame.get(size - 1));
                    stackFrame.add(size - 4, stackFrame.get(size - 1));
                    break;
                }
                case SWAP: {
                    size = stackFrame.size();
                    stackFrame.add(size - 2, stackFrame.get(size - 1));
                    stackFrame.remove(size);
                    break;
                }
            }
        } else {
            switch (opcode) {
                case RETURN:
                case IRETURN:
                case FRETURN:
                case ARETURN:
                case LRETURN:
                case DRETURN:
                case ATHROW: {
                    onMethodExit(insn);
                    break;
                }
            }
        }
    }

    @Override
    public void visitVarInsn(VarInsnNode insn) {
        int opcode = insn.getOpcode();
        if (constructor) {
            switch (opcode) {
                case ILOAD:
                case FLOAD: {
                    pushValue(OTHER);
                    break;
                }
                case LLOAD:
                case DLOAD: {
                    pushValue(OTHER);
                    pushValue(OTHER);
                    break;
                }
                case ALOAD: {
                    pushValue(insn.var == 0 ? THIS : OTHER);
                    break;
                }
                case ASTORE:
                case ISTORE:
                case FSTORE: {
                    popValue();
                    break;
                }
                case LSTORE:
                case DSTORE: {
                    popValue();
                    popValue();
                    break;
                }
            }
        }
    }

    @Override
    public void visitFieldInsn(FieldInsnNode insn) {
        if (constructor) {
            char c = insn.desc.charAt(0);
            boolean longOrDouble = c == 'J' || c == 'D';
            int opcode = insn.getOpcode();
            switch (opcode) {
                case GETSTATIC: {
                    pushValue(OTHER);
                    if (longOrDouble) {
                        pushValue(OTHER);
                    }
                    break;
                }
                case PUTSTATIC: {
                    popValue();
                    if (longOrDouble) {
                        popValue();
                    }
                    break;
                }
                case PUTFIELD: {
                    popValue();
                    if (longOrDouble) {
                        popValue();
                        popValue();
                    }
                    break;
                }
                // case GETFIELD:
                default: {
                    if (longOrDouble) {
                        pushValue(OTHER);
                    }
                }
            }
        }
    }

    @Override
    public void visitIntInsn(IntInsnNode insn) {
        if (constructor && insn.getOpcode() != NEWARRAY) {
            pushValue(OTHER);
        }
    }

    @Override
    public void visitLdcInsn(LdcInsnNode insn) {
        if (constructor) {
            pushValue(OTHER);
            if (insn.cst instanceof Double || insn.cst instanceof Long) {
                pushValue(OTHER);
            }
        }
    }

    @Override
    public void visitMultiANewArrayInsn(MultiANewArrayInsnNode insn) {
        if (constructor) {
            for (int i = 0; i < insn.dims; i++) {
                popValue();
            }
            pushValue(OTHER);
        }
    }

    @Override
    public void visitTypeInsn(TypeInsnNode insn) {
        // ANEWARRAY, CHECKCAST or INSTANCEOF don't change stack
        if (constructor && insn.getOpcode() == NEW) {
            pushValue(OTHER);
        }
    }

    @Override
    public void visitMethodInsn(MethodInsnNode insn) {
        if (constructor) {
            Type[] types = Type.getArgumentTypes(insn.desc);
            for (Type type : types) {
                popValue();
                if (type.getSize() == 2) {
                    popValue();
                }
            }
            int opcode = insn.getOpcode();
            switch (opcode) {
                // case INVOKESTATIC:
                // break;
                case INVOKEINTERFACE:
                case INVOKEVIRTUAL: {
                    popValue(); // objectref
                    break;
                }
                case INVOKESPECIAL: {
                    Object type = popValue(); // objectref
                    if (type == THIS && !superInitialized) {
                        onMethodEnter();
                        superInitialized = true;
                        // once super has been initialized it is no longer
                        // necessary to keep track of stack state
                        constructor = false;
                    }
                    break;
                }
            }
            Type returnType = Type.getReturnType(insn.desc);
            if (returnType != Type.VOID_TYPE) {
                pushValue(OTHER);
                if (returnType.getSize() == 2) {
                    pushValue(OTHER);
                }
            }
        }
    }

    @Override
    public void visitInvokeDynamicInsn(InvokeDynamicInsnNode insn) {
        if (constructor) {
            Type[] types = Type.getArgumentTypes(insn.desc);
            for (Type type : types) {
                popValue();
                if (type.getSize() == 2) {
                    popValue();
                }
            }
            Type returnType = Type.getReturnType(insn.desc);
            if (returnType != Type.VOID_TYPE) {
                pushValue(OTHER);
                if (returnType.getSize() == 2) {
                    pushValue(OTHER);
                }
            }
        }
    }

    @Override
    public void visitJumpInsn(JumpInsnNode insn) {
        int opcode = insn.getOpcode();
        if (constructor) {
            switch (opcode) {
                case IFEQ:
                case IFNE:
                case IFLT:
                case IFGE:
                case IFGT:
                case IFLE:
                case IFNULL:
                case IFNONNULL: {
                    popValue();
                    break;
                }
                case IF_ICMPEQ:
                case IF_ICMPNE:
                case IF_ICMPLT:
                case IF_ICMPGE:
                case IF_ICMPGT:
                case IF_ICMPLE:
                case IF_ACMPEQ:
                case IF_ACMPNE: {
                    popValue();
                    popValue();
                    break;
                }
                case JSR: {
                    pushValue(OTHER);
                    break;
                }
            }
            addBranch(insn.label);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitLookupSwitchInsn(LookupSwitchInsnNode insn) {
        if (constructor) {
            popValue();
            addBranches(insn.dflt, insn.labels);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitTableSwitchInsn(TableSwitchInsnNode insn) {
        if (constructor) {
            popValue();
            addBranches(insn.dflt, insn.labels);
        }
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
                                   String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        if (constructor && !branches.containsKey(handler)) {
            List<Object> stackFrame = new ArrayList<>();
            stackFrame.add(OTHER);
            branches.put(handler, stackFrame);
        }
    }

    protected void addBranches(LabelNode dflt, List<LabelNode> labels) {
        addBranch(dflt);
        labels.forEach(this::addBranch);
    }

    protected void addBranch(LabelNode labelNode) {
        Label label = labelNode.getLabel();
        if (branches.containsKey(label)) {
            return;
        }
        branches.put(label, new ArrayList<>(stackFrame));
    }

    protected Object popValue() {
        return stackFrame.remove(stackFrame.size() - 1);
    }

    protected Object peekValue() {
        return stackFrame.get(stackFrame.size() - 1);
    }

    protected void pushValue(Object o) {
        stackFrame.add(o);
    }

    protected void onMethodEnter() {
    }

    protected void onMethodExit(InsnNode insn) {
        if (insn.getOpcode() != ATHROW) {
            AbstractInsnNode previous = insn.getPrevious();
            if (previous != null && previous instanceof LabelNode) {
                exits.add(insn);
            }
        }
    }

    @Override
    public final void visitEnd() {
        if (!exits.isEmpty()) {
            exits.sort((a, b) -> {
                int goA = method.instructions().indexOf(((JumpInsnNode) a.getPrevious().getPrevious()).label);
                int goB = method.instructions().indexOf(((JumpInsnNode) a.getPrevious().getPrevious()).label);
                return (goB - goA);
            });
            visitExit(exits.get(0));
        }
    }

    public abstract void visitExit(InsnNode insn);
}
