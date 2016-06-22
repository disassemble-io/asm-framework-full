package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.visitor.expr.node.*;

import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public class MultiExprTreeVisitor extends ExprTreeVisitor {

    private final List<ExprTreeVisitor> visitors;

    public MultiExprTreeVisitor(List<ExprTreeVisitor> visitors) {
        this.visitors = visitors;
    }

    public final void visitExpr(BasicExpr expr) {
        visitors.forEach(visitor -> visitor.visitExpr(expr));
    }

    public final void visitBasicExpr(BasicExpr expr) {
        visitors.forEach(visitor -> visitor.visitBasicExpr(expr));
    }

    public final void visitMathExpr(MathExpr expr) {
        visitors.forEach(visitor -> visitor.visitMathExpr(expr));
    }

    public final void visitConstExpr(ConstExpr expr) {
        visitors.forEach(visitor -> visitor.visitConstExpr(expr));
    }

    public final void visitFieldExpr(FieldExpr expr) {
        visitors.forEach(visitor -> visitor.visitFieldExpr(expr));
    }

    public final void visitCompBranchExpr(CompBranchExpr expr) {
        visitors.forEach(visitor -> visitor.visitCompBranchExpr(expr));
    }

    public final void visitBranchExpr(BranchExpr expr) {
        visitors.forEach(visitor -> visitor.visitBranchExpr(expr));
    }

    public final void visitVarLoadExpr(VarLoadExpr expr) {
        visitors.forEach(visitor -> visitor.visitVarLoadExpr(expr));
    }

    public final void visitVarStoreExpr(VarStoreExpr expr) {
        visitors.forEach(visitor -> visitor.visitVarStoreExpr(expr));
    }

    public final void visitVarExpr(VarExpr expr) {
        visitors.forEach(visitor -> visitor.visitVarExpr(expr));
    }

    public final void visitStart(ExprTree tree) {
        visitors.forEach(visitor -> visitor.visitStart(tree));
    }

    public final void visitEnd(ExprTree tree) {
        visitors.forEach(visitor -> visitor.visitEnd(tree));
    }
}
