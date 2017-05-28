package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public abstract class MemberExpr<T extends AbstractInsnNode> extends BasicExpr {

    /**
     * Constructs a BasicExpr for the given instruction and type.
     *
     * @param method The method this expression is in.
     * @param insn   The instruction to use.
     * @param index  The index of this instruction in the reverse stack.
     * @param size   The amount of slots taken up by this instruction.
     */
    public MemberExpr(ClassMethod method, AbstractInsnNode insn, int index, int size) {
        super(method, insn, index, size);
    }

    /**
     * Retrieves this members's reference key. (ownerName.memberName)
     *
     * @return This members's reference key.
     */
    public abstract String key();

    /**
     * Retrieves this member's owner.
     *
     * @return This member's owner.
     */
    public abstract String owner();

    /**
     * Retrieves this member's name.
     *
     * @return This member's name.
     */
    public abstract String name();

    /**
     * Retrieves this member's descriptor.
     *
     * @return This member's descriptor.
     */
    public abstract String desc();
}
