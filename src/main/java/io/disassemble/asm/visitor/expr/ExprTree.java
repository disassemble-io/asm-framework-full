package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.expr.grep.GrepExpr;
import io.disassemble.asm.visitor.expr.node.BasicExpr;

import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 * <p>
 * TODO: add WhileLoopExpr/ForLoopExpr
 */
public class ExprTree implements Iterable<BasicExpr> {

    public static final String VERBOSE_EXPRESSION_TREE = "ExprTree#VERBOSE_EXPRESSION_TREE";

    private final ClassMethod method;
    private final Deque<BasicExpr> expressions;

    /**
     * Constructs an ExprTree based on the given expressions.
     *
     * @param expressions The expressions to build an ExprTree for.
     */
    public ExprTree(ClassMethod method, Deque<BasicExpr> expressions) {
        this.method = method;
        this.expressions = expressions;
    }

    /**
     * Retrieves the expressions used to construct this ExprTree.
     *
     * @return The expression used to construct this ExprTree.
     */
    public Deque<BasicExpr> expressions() {
        return expressions;
    }

    /**
     * Obtains the ClassMethod that this ExprTree is constructed for.
     *
     * @return The ClassMethod that this ExprTree is constructed for.
     */
    public ClassMethod method() {
        return method;
    }

    /**
     * Pretty-prints this ExprTree.
     */
    public void print() {
        expressions.forEach(BasicExpr::print);
    }

    @SuppressWarnings("unchecked")
    private void accept(ExprTreeVisitor visitor, BasicExpr parent) {
        visitor.visitExpr(parent);
        parent.forEach(expr -> accept(visitor, expr));
    }

    /**
     * Visits every BasicExpr in the tree.
     *
     * @param visitor The visitor to dispatch.
     */
    public void accept(ExprTreeVisitor visitor) {
        visitor.visitStart(this);
        for (BasicExpr expr : this) {
            accept(visitor, expr);
        }
        visitor.visitEnd(this);
    }

    @Override
    public Iterator<BasicExpr> iterator() {
        return expressions.iterator();
    }

    /**
     * Greps for a match within this ExprTree.
     * <p>
     * This should not be used if speed is an issue, but rather be executed in parallel.
     *
     * @param pattern The basic grep pattern to be used.
     *                <p>
     *                String test = "This is a test";
     *                String pattern = "This {word} a test";
     *                ... 'is'
     * @param type The type of BasicExpr.
     * @param consumer The consumer of mapped values.
     */
    public void grep(String pattern, Class<?> type, Consumer<Map<String, String>> consumer) {
        accept(GrepExpr.createVisitor(Collections.singletonList(new GrepExpr(pattern, type, consumer))));
    }
}
