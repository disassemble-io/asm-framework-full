package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * A BasicExpr that represents a branch.
 */
public class BranchExpr extends BasicExpr<JumpInsnNode> {

    public BranchExpr(ClassMethod method, JumpInsnNode insn, int type) {
        super(method, insn, type);
    }
}
