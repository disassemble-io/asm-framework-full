package me.sedlar.asm.visitor.flow;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/26/2016
 */
public class BasicInstruction {

    public final BasicBlock block;
    public final AbstractInsnNode insn;

    protected BasicInstruction previous;

    public BasicInstruction(BasicBlock block, AbstractInsnNode insn) {
        this.block = block;
        this.insn = insn;
    }

    /**
     * Gets the BasicInstruction prior to this instruction.
     *
     * @return The BasicInstruction prior to this instruction.
     */
    public BasicInstruction previous() {
        return (block.instructions.size() > 1 ? block.instructions.get(block.instructions.indexOf(this) - 1) : null);
    }

    /**
     * Gets the BasicInstruction after this instruction.
     *
     * @return The BasicInstruction after this instruction.
     */
    public BasicInstruction next() {
        int idx = block.instructions.indexOf(this);
        return ((idx + 1) < block.instructions.size() ? block.instructions.get(idx + 1) : null);
    }

    /**
     * Gets the predecessor block's ending instruction.
     *
     * @return The predecessor block's ending instruction.
     */
    public BasicInstruction parent() {
        return (block.predecessor != null ? block.predecessor.endInstruction().orElse(null) : null);
    }

    @Override
    public int hashCode() {
        return insn.hashCode();
    }
}
