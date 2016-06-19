package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.visitor.expr.node.*;

/**
 * @author Tyler Sedlar
 * @since 6/18/16
 */
public class ExprTreeVisitor {

    /**
     * The handler that redirects visit calls.
     *
     * @param expr The expression to be visited.
     */
    public void visitExpr(BasicExpr expr) {
        if (expr instanceof MathExpr) {
            visitMathExpr((MathExpr) expr);
        } else if (expr instanceof ConstExpr) {
            visitConstExpr((ConstExpr) expr);
        } else if (expr instanceof FieldExpr) {
            visitFieldExpr((FieldExpr) expr);
        } else if (expr instanceof CompBranchExpr) {
            visitCompBranchExpr((CompBranchExpr) expr);
        } else if (expr instanceof BranchExpr) {
            visitBranchExpr((BranchExpr) expr);
        } else {
            visitBasicExpr(expr);
        }
    }

    /**
     * Visits a BasicExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitBasicExpr(BasicExpr expr) {
    }

    /**
     * Visits a MathExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitMathExpr(MathExpr expr) {
    }

    /**
     * Visits a ConstExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitConstExpr(ConstExpr expr) {
    }

    /**
     * Visits a FieldExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitFieldExpr(FieldExpr expr) {
    }

    /**
     * Visits a CompBranchExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitCompBranchExpr(CompBranchExpr expr) {
    }

    /**
     * Visits a BranchExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitBranchExpr(BranchExpr expr) {
    }
}
