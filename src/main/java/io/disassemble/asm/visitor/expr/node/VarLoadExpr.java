package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public class VarLoadExpr extends VarExpr {

    public VarLoadExpr(ClassMethod method, VarInsnNode insn, int type) {
        super(method, insn, type);
    }
}
