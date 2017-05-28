package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public class VarStoreExpr extends VarExpr {

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param method The method this expression is in.
     * @param insn   The instruction to use.
     * @param index  The index of this instruction in the reverse stack.
     * @param size   The amount of slots taken up by this instruction.
     */
    public VarStoreExpr(ClassMethod method, VarInsnNode insn, int index, int size) {
        super(method, insn, index, size);
    }
}
