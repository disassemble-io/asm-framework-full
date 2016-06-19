package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/18/16
 */
public class MethodExpr extends BasicExpr {

    public MethodExpr(ClassMethod method, MethodInsnNode insn, int type) {
        super(method, insn, type);
    }
}
