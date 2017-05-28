package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.FieldInsnNode;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 *
 * A BasicExpr that represents a field.
 */
public class FieldExpr extends MemberExpr<FieldInsnNode> {

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param method The method this expression is in.
     * @param insn   The instruction to use.
     * @param index  The index of this instruction in the reverse stack.
     * @param size   The amount of slots taken up by this instruction.
     */
    public FieldExpr(ClassMethod method, FieldInsnNode insn, int index, int size) {
        super(method, insn, index, size);
    }

    @Override
    public String key() {
        return (owner() + '.' + name());
    }

    @Override
    public String owner() {
        return ((FieldInsnNode) insn).owner;
    }

    @Override
    public String name() {
        return ((FieldInsnNode) insn).name;
    }

    @Override
    public String desc() {
        return ((FieldInsnNode) insn).desc;
    }

    /**
     * Checks whether this field is a GETFIELD or GETSTATIC instruction.
     *
     * @return <tt>true</tt> if this field is a GETFIELD or GETSTATIC instruction, otherwise <tt>false</tt>.
     */
    public boolean getter() {
        return (opcode() == GETFIELD || opcode() == GETSTATIC);
    }

    /**
     * Checks whether this field is a PUTFIELD or PUTSTATIC instruction.
     *
     * @return <tt>true</tt> if this field is a PUTFIELD or PUTSTATIC instruction, otherwise <tt>false</tt>.
     */
    public boolean putter() {
        return !getter();
    }
}
