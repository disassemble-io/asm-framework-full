package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public class VarExpr extends BasicExpr {

    public VarExpr(ClassMethod method, VarInsnNode insn, int type) {
        super(method, insn, type);
    }

    /**
     * Gets the variable index this expression is acting on.
     *
     * @return The variable index this expression is acting on.
     */
    public int var() {
        return ((VarInsnNode) insn).var;
    }
}
