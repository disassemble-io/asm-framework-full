package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 */
public class ExpressionVisitor extends InterpretingVisitor {

    private Deque<BasicExpr> stack = new ArrayDeque<>();

    public void visitExpr(BasicExpr stack) {
    }

    private void handleInsn(AbstractInsnNode insn, int type) {
        BasicExpr expr = BasicExpr.resolve(insn, type);
        if (!stack.isEmpty()) {
            BasicExpr peek = stack.peekLast();
            expr.setLeft(peek);
            peek.setRight(expr);
            BasicExpr.setExtras(peek);
            visitExpr(peek);
        }
        stack.add(expr);
    }

    @Override
    public void visitEnd() {
        if (!stack.isEmpty()) {
            visitExpr(stack.peekLast());
        }
        stack = new ArrayDeque<>();
    }

    @Override
    public void visitBinaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_BINARY);
    }

    @Override
    public void visitCopyOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_COPY);
    }

    @Override
    public void visitNaryOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_NARY);
    }

    @Override
    public void visitNewOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_NEW);
    }

    @Override
    public void visitReturnOperation(AbstractInsnNode insn) {
        handleInsn(insn, BasicExpr.OP_RETURN);
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
