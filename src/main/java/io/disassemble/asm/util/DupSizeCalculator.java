package io.disassemble.asm.util;

import io.disassemble.asm.visitor.expr.ExprTreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 5/28/2017
 */
public class DupSizeCalculator {

    public static int dup2(AbstractInsnNode insn) {
        boolean size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious());
        if (!size2) {
            size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious());
            if (!size2) {
                return 2;
            }
        } else {
            return 1;
        }
        throw new IllegalStateException("Illegal use of DUP2 @ " + insn);
    }

    public static int dup_x2(AbstractInsnNode insn) {
        boolean size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious());
        if (!size2) {
            size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious());
            if (!size2) {
                size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious().getPrevious());
                if (!size2) {
                    return 3;
                }
            } else {
                return 2;
            }
        }
        throw new IllegalStateException("Illegal use of DUP_X2 @ " + insn);
    }

    public static int dup2_x1(AbstractInsnNode insn) {
        boolean size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious());
        if (!size2) {
            size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious());
            if (!size2) {
                size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious().getPrevious());
                if (!size2) {
                    return 3;
                }
            }
        } else {
            size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious());
            if (!size2) {
                return 2;
            } else {
                return 3;
            }
        }
        throw new IllegalStateException("Illegal use of DUP2_X1 @ " + insn);
    }

    public static int dup2_x2(AbstractInsnNode insn) {
        boolean size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious());
        if (!size2) {
            size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious());
            if (!size2) {
                size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious().getPrevious());
                if (!size2) {
                    size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious().getPrevious().getPrevious());
                    if (!size2) {
                        return 4;
                    }
                } else {
                    return 3;
                }
            }
        } else {
            size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious());
            if (!size2) {
                size2 = ExprTreeBuilder.isDoubleOrLong(insn.getPrevious().getPrevious().getPrevious());
                if (!size2) {
                    return 3;
                }
            } else {
                return 2;
            }
        }
        throw new IllegalStateException("Illegal use of DUP2_X2 @ " + insn);
    }
}
