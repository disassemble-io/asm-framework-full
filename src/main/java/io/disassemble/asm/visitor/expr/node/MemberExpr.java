package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public abstract class MemberExpr<T extends AbstractInsnNode> extends BasicExpr<T> {

    public MemberExpr(ClassMethod method, T insn, int type) {
        super(method, insn, type);
    }

    /**
     * Retrieves this field's reference key. (owner.field)
     *
     * @return This field's reference key.
     */
    public abstract String key();
}
