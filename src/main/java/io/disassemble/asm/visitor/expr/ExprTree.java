package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 *
 * TODO: add WhileLoopExpr/ForLoopExpr
 */
public class ExprTree implements Iterable<BasicExpr> {

    private static final Map<String, List<BasicExpr>> EXPRS = new HashMap<>();

    private static final ExpressionVisitor EXPR_VISITOR = new ExpressionVisitor() {
        public void visitExpr(BasicExpr expr) {
            String key = method.key();
            if (!EXPRS.containsKey(key)) {
                EXPRS.put(key, new ArrayList<>());
            }
            EXPRS.get(key).add(expr);
        }
    };

    private final Deque<BasicExpr> expressions;

    private ClassMethod method;

    /**
     * Constructs an ExprTree based on the given expressions.
     *
     * @param expressions The expressions to build an ExprTree for.
     */
    public ExprTree(Deque<BasicExpr> expressions) {
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

    private void accept(ExprTreeVisitor visitor, BasicExpr parent) {
        visitor.visitExpr(parent);
        for (BasicExpr child : parent) {
            accept(visitor, child);
        }
    }

    /**
     * Visits every BasicExpr in the tree.
     *
     * @param visitor The visitor to dispatch.
     */
    public void accept(ExprTreeVisitor visitor) {
        for (BasicExpr expr : this) {
            accept(visitor, expr);
        }
    }

    @Override
    public Iterator<BasicExpr> iterator() {
        return expressions.iterator();
    }

    private static void handleExpr(Iterator<BasicExpr> itr, BasicExpr expr, BasicExpr parent) {
        if (parent != null) {
            parent.addChild(expr);
        }
        // the amount of instructions that need to be 'popped'
        int proceeding = expr.proceeding();
        BasicExpr prev = null;
        for (int i = 0; i < proceeding; i++) {
            BasicExpr child = itr.next();
            if (prev != null) {
                // setRight/setLeft instead of setLeft/setRight due to Collections#reverse in #build
                child.setRight(prev);
                prev.setLeft(child);
            }
            handleExpr(itr, child, expr);
            prev = child;
            // determine the amount of instructions to skip
            i += consume(expr.insn);
        }
    }

    /**
     * Builds an ExprTree for the given ClassMethod.
     *
     * @param method The ClassMethod to build an ExprTree for.
     * @return An Optional for the built tree, if it exists, otherwise an empty value.
     */
    public static Optional<ExprTree> build(ClassMethod method) {
        method.accept(EXPR_VISITOR);
        Optional<ExprTree> treeOpt = build(EXPRS.get(method.key()));
        treeOpt.ifPresent(tree -> tree.method = method);
        return treeOpt;
    }

    /**
     * Builds a map of ExprTrees for the given classes.
     *
     * @param classes The classes to build ExprTrees for.
     * @return A map of ExprTrees for the given classes.
     */
    public static Map<String, Deque<ExprTree>> buildAll(Map<String, ClassFactory> classes) {
        Map<String, Deque<ExprTree>> trees = new HashMap<>();
        classes.values().forEach(factory -> {
            Deque<ExprTree> factoryTrees = new ArrayDeque<>();
            for (ClassMethod method : factory.methods) {
                build(method).ifPresent(factoryTrees::add);
            }
            trees.put(factory.name(), factoryTrees);
        });
        return trees;
    }

    /**
     * Builds an ExprTree for the given List<BasicExpr>
     *
     * @param exprList The list of BasicExpr to build an ExprTree for.
     * @return An Optional for the built tree, if it exists, otherwise an empty value.
     */
    private static Optional<ExprTree> build(List<BasicExpr> exprList) {
        if (exprList == null) {
            return Optional.empty();
        }
        // Reverse the list to handle instructions by 'stack' order.
        Collections.reverse(exprList);
        Iterator<BasicExpr> itr = exprList.iterator();
        Deque<BasicExpr> exprs = new ArrayDeque<>();
        BasicExpr prev = null;
        while (itr.hasNext()) {
            BasicExpr expr = itr.next();
            if (prev != null) {
                // setRight/setLeft instead of setLeft/setRight due to Collections#reverse
                expr.setRight(prev);
                prev.setLeft(expr);
            }
            // recursively handle expressions, so if the next needs to pop more instructions off,
            // it can do so while handling heritage correctly.
            handleExpr(itr, expr, null);
            // add to the front of the Deque to accomodate for calling Collections#reverse
            exprs.addFirst(expr);
            prev = expr;
        }
        return Optional.of(new ExprTree(exprs));
    }

    /**
     * Retrieves the amount of instructions that should be consumed by the given instruction.
     *
     * @param insn The instruction to retrieve the consume size for.
     * @return The amount of instructions that should be consumed by the given instruction.
     */
    private static int consume(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op == DUP_X1 || op == DUP2) {
            return 2;
        } else if (op == DUP_X2 || op == DUP2_X1) {
            return 3;
        } else if (op == DUP2_X2) {
            return 4;
        }
        return 0;
    }
}
