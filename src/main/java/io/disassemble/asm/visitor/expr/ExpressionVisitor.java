package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 */
public class ExpressionVisitor extends InterpretingVisitor {

    public void visitExpr(BasicExpr expr) {
    }

    @SuppressWarnings("unchecked")
    private void handleInsn(AbstractInsnNode insn) {
        int reverseStackIdx = (method.instructions().size() - method.instructions().indexOf(insn));
        int size = ExprTreeBuilder.resolveSize(insn);
        BasicExpr expr = BasicExpr.resolve(method, insn, reverseStackIdx, size);
        visitExpr(expr);
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitBinaryOperation(AbstractInsnNode insn) {
        handleInsn(insn);
    }

    @Override
    public void visitNaryOperation(AbstractInsnNode insn) {
        handleInsn(insn);
    }

    @Override
    public void visitNullaryOperation(AbstractInsnNode insn) {
        handleInsn(insn);
    }

    @Override
    public void visitQuaternaryOperation(AbstractInsnNode insn) {
        handleInsn(insn);
    }

    @Override
    public void visitTernaryOperation(AbstractInsnNode insn) {
        handleInsn(insn);
    }

    @Override
    public void visitUnaryOperation(AbstractInsnNode insn) {
        handleInsn(insn);
    }
}
