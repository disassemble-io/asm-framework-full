package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.util.Grep;
import io.disassemble.asm.visitor.expr.grep.GrepExpr;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import io.disassemble.asm.visitor.expr.node.MethodExpr;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.*;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 * <p>
 * TODO: add WhileLoopExpr/ForLoopExpr
 */
public class ExprTree implements Iterable<BasicExpr<AbstractInsnNode>> {

    public static final String VERBOSE_EXPRESSION_TREE = "ExprTree#VERBOSE_EXPRESSION_TREE";

    private final ClassMethod method;
    private final Deque<BasicExpr<AbstractInsnNode>> expressions;

    /**
     * Constructs an ExprTree based on the given expressions.
     *
     * @param expressions The expressions to build an ExprTree for.
     */
    public ExprTree(ClassMethod method, Deque<BasicExpr<AbstractInsnNode>> expressions) {
        this.method = method;
        this.expressions = expressions;
    }

    /**
     * Retrieves the expressions used to construct this ExprTree.
     *
     * @return The expression used to construct this ExprTree.
     */
    public Deque<BasicExpr<AbstractInsnNode>> expressions() {
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
    private void accept(ExprTreeVisitor visitor, BasicExpr<AbstractInsnNode> parent) {
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
        for (BasicExpr<AbstractInsnNode> expr : this) {
            accept(visitor, expr);
        }
        visitor.visitEnd(this);
    }

    @Override
    public Iterator<BasicExpr<AbstractInsnNode>> iterator() {
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
