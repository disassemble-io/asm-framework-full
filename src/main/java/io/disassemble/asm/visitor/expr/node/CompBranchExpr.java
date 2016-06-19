package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * A BranchExpr that represents a comparison branch.
 */
public class CompBranchExpr extends BranchExpr {

    public CompBranchExpr(ClassMethod method, AbstractInsnNode insn, int type) {
        super(method, insn, type);
    }
}
