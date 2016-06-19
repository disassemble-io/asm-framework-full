package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * A BranchExpr that represents a comparison branch.
 */
public class CompBranchExpr extends BranchExpr {

    public CompBranchExpr(AbstractInsnNode insn, int type) {
        super(insn, type);
    }
}
