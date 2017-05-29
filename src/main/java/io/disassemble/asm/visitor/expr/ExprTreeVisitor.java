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
        } else if (expr instanceof MethodExpr) {
            visitMethodExpr((MethodExpr) expr);
        } else if (expr instanceof CompBranchExpr) {
            visitCompBranchExpr((CompBranchExpr) expr);
        } else if (expr instanceof BranchExpr) {
            visitBranchExpr((BranchExpr) expr);
        } else if (expr instanceof VarLoadExpr) {
            visitVarLoadExpr((VarLoadExpr) expr);
        } else if (expr instanceof VarStoreExpr) {
            visitVarStoreExpr((VarStoreExpr) expr);
        } else if (expr instanceof VarExpr) {
            visitVarExpr((VarExpr) expr);
        } else if (expr instanceof PushExpr) {
            visitPushExpr((PushExpr) expr);
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
     * Visits a MethodExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitMethodExpr(MethodExpr expr) {
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

    /**
     * Visits a VarLoadExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitVarLoadExpr(VarLoadExpr expr) {
    }

    /**
     * Visits a VarStoreExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitVarStoreExpr(VarStoreExpr expr) {
    }

    /**
     * Visits a VarExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitVarExpr(VarExpr expr) {
    }

    /**
     * Visits a PushExpr.
     *
     * @param expr The expression to be visited.
     */
    public void visitPushExpr(PushExpr expr) {
    }

    /**
     * Callable upon the beginning of visiting an ExprTree.
     *
     * @param tree The tree that is currently being visited.
     */
    public void visitStart(ExprTree tree) {
    }

    /**
     * Callable upon the end of visiting an ExprTree.
     *
     * @param tree The tree that is currently being visited.
     */
    public void visitEnd(ExprTree tree) {
    }
}
