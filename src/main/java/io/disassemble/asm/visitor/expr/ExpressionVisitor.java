package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 */
public class ExpressionVisitor extends InterpretingVisitor {

    public void visitExpr(BasicExpr<AbstractInsnNode> expr) {
    }

    @SuppressWarnings("unchecked")
    private void handleInsn(AbstractInsnNode insn, int type) {
        BasicExpr<AbstractInsnNode> expr = BasicExpr.resolve(method, insn, type);
        visitExpr(expr);
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitBinaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_BINARY);
    }

    @Override
    public void visitNaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_NARY);
    }

    @Override
    public void visitNullaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_NULLARY);
    }

    @Override
    public void visitQuaternaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_QUATERNARY);
    }

    @Override
    public void visitTernaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_TERNARY);
    }

    @Override
    public void visitUnaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_UNARY);
    }
}
