package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * A BasicExpr that represents a branch.
 */
public class BranchExpr extends BasicExpr {

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param method The method this expression is in.
     * @param insn   The instruction to use.
     * @param index  The index of this instruction in the reverse stack.
     * @param size   The amount of slots taken up by this instruction.
     */
    public BranchExpr(ClassMethod method, JumpInsnNode insn, int index, int size) {
        super(method, insn, index, size);
    }
}
