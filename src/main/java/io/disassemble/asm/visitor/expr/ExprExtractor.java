package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/24/16
 */
public class ExprExtractor {

    private static final String EMPTY_STRING = "";

    public static String extract(BasicExpr<AbstractInsnNode> expr) {
        AbstractInsnNode insn = expr.insn();
        if (insn instanceof IntInsnNode) {
            int operand = ((IntInsnNode) insn).operand;
            return Integer.toString(operand);
        } else if (insn instanceof LdcInsnNode) {
            Object ldc = ((LdcInsnNode) insn).cst;
            return (ldc == null ? "null" : ldc.toString());
        }
        return EMPTY_STRING;
    }
}
