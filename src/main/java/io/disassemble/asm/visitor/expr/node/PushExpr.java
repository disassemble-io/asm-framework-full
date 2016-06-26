package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/26/16
 */
public class PushExpr extends BasicExpr<AbstractInsnNode> {

    private boolean intInsn;

    public PushExpr(ClassMethod method, AbstractInsnNode insn, int type) {
        super(method, insn, type);
        intInsn = (opcode() == BIPUSH || opcode() == SIPUSH);
    }

    /**
     * Gets the number that this expression is pushing onto the stack.
     *
     * @return The number that this expression is pushing onto the stack.
     */
    public int number() {
        if (intInsn) {
            return asIntInsn().operand;
        } else {
            if (opcode() == ICONST_M1) {
                return -1;
            } else if (opcode() == ICONST_0) {
                return 0;
            } else if (opcode() == ICONST_1) {
                return 1;
            } else if (opcode() == ICONST_2) {
                return 2;
            } else if (opcode() == ICONST_3) {
                return 3;
            } else if (opcode() == ICONST_4) {
                return 4;
            } else if (opcode() == ICONST_5) {
                return 5;
            }
        }
        throw new IllegalStateException("Opcode is not BIPUSH, SIPUSH, or ICONST_*");
    }

    /**
     * Retrieves this expression's instruction as an IntInsnNode.
     *
     * @return This expression's instruction as an IntInsnNode.
     */
    public IntInsnNode asIntInsn() {
        if (!intInsn) {
            throw new IllegalStateException("Instruction is not an IntInsnNode");
        }
        return (IntInsnNode) insn;
    }
}
