package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.LdcInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 *
 * A BasicExpr that represents a constant.
 */
public class ConstExpr extends BasicExpr {

    private final LdcInsnNode ldc;

    public ConstExpr(LdcInsnNode ldc, int type) {
        super(ldc, type);
        this.ldc = ldc;
    }

    /**
     * Checks whether this constant is a number or not.
     *
     * @return <t>true</t> if this constant is a number, otherwise <t>false</t>.
     */
    public boolean isNumber() {
        return ldc.cst instanceof Number;
    }

    /**
     * Gets the number of this constant if it exists, otherwise null.
     *
     * @return The number of this constant if it exists, otherwise <t>null</t>.
     */
    public Number number() {
        if (!isNumber()) {
            return null;
        }
        return (Number) ldc.cst;
    }
}
