package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public abstract class MemberExpr<T extends AbstractInsnNode> extends BasicExpr {

    public MemberExpr(ClassMethod method, T insn, int type) {
        super(method, insn, type);
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
