package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 */
class ExpressionVisitor extends InterpretingVisitor {

    private Deque<BasicExpr> stack = new ArrayDeque<>();

    public void visitExpr(BasicExpr stack) {
    }

    private void handleInsn(AbstractInsnNode insn, int type) {
        BasicExpr expr = BasicExpr.resolve(method, insn, type);
        visitExpr(expr);
        stack.add(expr);
    }

    @Override
    public void visitEnd() {
        if (stack.isEmpty()) {
            stack = new ArrayDeque<>();
        }
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
