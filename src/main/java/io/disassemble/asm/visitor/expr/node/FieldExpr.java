package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.FieldInsnNode;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class FieldExpr extends BasicExpr {

    private final FieldInsnNode field;

    public FieldExpr(FieldInsnNode field, int type) {
        super(field, type);
        this.field = field;
    }

    public String key() {
        return (field.owner + "." + field.name);
    }

    public boolean getter() {
        return (opcode() == GETFIELD || opcode() == GETSTATIC);
    }

    public boolean putter() {
        return !getter();
    }
}
