package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.LdcInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class ConstExpr extends BasicExpr {

    private final LdcInsnNode ldc;

    public ConstExpr(LdcInsnNode ldc, int type) {
        super(ldc, type);
        this.ldc = ldc;
    }

    public Number number() {
        return (Number) ldc.cst;
    }
}
