package io.disassemble.asm.visitor.flow;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 4/8/16
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
        return (block.size() > 1 ? block.get(block.indexOf(this) - 1) : null);
    }

    /**
     * Gets the BasicInstruction after this instruction.
     *
     * @return The BasicInstruction after this instruction.
     */
    public BasicInstruction next() {
        int idx = block.indexOf(this);
        return ((idx + 1) < block.size() ? block.get(idx + 1) : null);
    }

    /**
     * Gets the predecessor block's ending instruction.
     *
     * @return The predecessor block's ending instruction.
     */
    public BasicInstruction parent() {
        return (block.predecessor != null ? block.predecessor.exit() : null);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BasicInstruction && insn.getOpcode() == ((BasicInstruction) o).insn.getOpcode() && block.equals(((BasicInstruction) o).block);
    }

    @Override
    public int hashCode() {
        int result = block.hashCode();
        result = 31 * result + insn.getOpcode();
        return result;
    }
}
