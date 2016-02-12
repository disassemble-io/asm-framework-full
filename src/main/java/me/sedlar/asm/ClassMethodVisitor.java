package me.sedlar.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * @author Tyler Sedlar
 * @since 3/8/15
 */
public class ClassMethodVisitor extends MethodVisitor implements Opcodes {

    private int idx = 0;
    private boolean locked = false;

    protected ClassMethod method;

    public ClassMethodVisitor() {
        super(Opcodes.ASM5, null);
    }

    private AbstractInsnNode current() {
        return method.instructions().get(idx++);
    }

    /**
     * Restricts this visitor from visiting any instructions.
     */
    public void lock() {
        locked = true;
    }

    /**
     * Allows this visitor to visit instructions.
     */
    public void unlock() {
        locked = false;
    }

    /**
     * Checks whether this visitor is locked or not.
     *
     * @return <t>true</t> if this visitor is locked, otherwise <t>false</t>.
     */
    public boolean locked() {
        return locked;
    }

    public void visitCode() {
        idx = 0;
    }

    public void visitFrame(FrameNode fn) {

    }

    public final void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (locked) {
            return;
        }
        visitFrame((FrameNode) current());
    }

    public void visitInsn(InsnNode in) {

    }

    public final void visitInsn(int opcode) {
        if (locked) {
            return;
        }
        visitInsn((InsnNode) current());
    }

    public void visitIntInsn(IntInsnNode iin) {

    }

    public final void visitIntInsn(int opcode, int operand) {
        if (locked) {
            return;
        }
        visitIntInsn((IntInsnNode) current());
    }

    public void visitVarInsn(VarInsnNode vin) {

    }

    public final void visitVarInsn(int opcode, int var) {
        if (locked) {
            return;
        }
        visitVarInsn((VarInsnNode) current());
    }

    public void visitTypeInsn(TypeInsnNode tin) {

    }

    public final void visitTypeInsn(int opcode, String type) {
        if (locked) {
            return;
        }
        visitTypeInsn((TypeInsnNode) current());
    }

    public void visitFieldInsn(FieldInsnNode fin) {

    }

    public final void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (locked) {
            return;
        }
        visitFieldInsn((FieldInsnNode) current());
    }

    public void visitMethodInsn(MethodInsnNode min) {

    }

    public final void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (locked) {
            return;
        }
        visitMethodInsn((MethodInsnNode) current());
    }

    public void visitInvokeDynamicInsn(InvokeDynamicInsnNode idin) {

    }

    public final void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        if (locked) {
            return;
        }
        visitInvokeDynamicInsn((InvokeDynamicInsnNode) current());
    }

    public void visitJumpInsn(JumpInsnNode jin) {

    }

    public final void visitJumpInsn(int opcode, Label label) {
        if (locked) {
            return;
        }
        visitJumpInsn((JumpInsnNode) current());
    }

    public void visitLabel(LabelNode ln) {

    }

    public final void visitLabel(Label label) {
        if (locked) {
            return;
        }
        visitLabel((LabelNode) current());
    }

    public void visitLdcInsn(LdcInsnNode lin) {

    }

    public final void visitLdcInsn(Object cst) {
        if (locked) {
            return;
        }
        visitLdcInsn((LdcInsnNode) current());
    }

    public void visitIincInsn(IincInsnNode iin) {

    }

    public final void visitIincInsn(int var, int increment) {
        if (locked) {
            return;
        }
        visitIincInsn((IincInsnNode) current());
    }

    public void visitTableSwitchInsn(TableSwitchInsnNode tsin) {

    }

    public final void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        if (locked) {
            return;
        }
        visitTableSwitchInsn((TableSwitchInsnNode) current());
    }

    public void visitLookupSwitchInsn(LookupSwitchInsnNode lsin) {

    }

    public final void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (locked) {
            return;
        }
        visitLookupSwitchInsn((LookupSwitchInsnNode) current());
    }

    public void visitMultiANewArrayInsn(MultiANewArrayInsnNode manai) {

    }

    public final void visitMultiANewArrayInsn(String desc, int dims) {
        if (locked) {
            return;
        }
        visitMultiANewArrayInsn((MultiANewArrayInsnNode) current());
    }

    public void visitLineNumber(LineNumberNode lnn) {

    }

    public final void visitLineNumber(int line, Label start) {
        if (locked) {
            return;
        }
        visitLineNumber((LineNumberNode) current());
    }
}