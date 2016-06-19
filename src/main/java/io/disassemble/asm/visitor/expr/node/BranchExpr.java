package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * A BasicExpr that represents a branch.
 */
public class BranchExpr extends BasicExpr {

    public BranchExpr(AbstractInsnNode insn, int type) {
        super(insn, type);
    }
}
