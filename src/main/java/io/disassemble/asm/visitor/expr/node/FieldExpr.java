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

    public FieldExpr(ClassMethod method, FieldInsnNode insn, int type) {
        super(method, insn, type);
    }

    @Override
    public String key() {
        return (insn.owner + '.' + insn.name);
    }

    @Override
    public String owner() {
        return insn.owner;
    }

    @Override
    public String name() {
        return insn.name;
    }

    @Override
    public String desc() {
        return insn.desc;
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
