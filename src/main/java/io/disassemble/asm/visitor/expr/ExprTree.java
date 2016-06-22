package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import io.disassemble.asm.visitor.expr.node.MethodExpr;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 * <p>
 * TODO: add WhileLoopExpr/ForLoopExpr
 */
public class ExprTree implements Iterable<BasicExpr<AbstractInsnNode>> {

    public static final String VERBOSE_EXPRESSION_TREE = "ExprTree#VERBOSE_EXPRESSION_TREE";

    private static final Map<String, List<BasicExpr<AbstractInsnNode>>> EXPRS = new HashMap<>();

    private static final ExpressionVisitor EXPR_VISITOR = new ExpressionVisitor() {
        public void visitExpr(BasicExpr<AbstractInsnNode> expr) {
            String key = method.key();
            if (!EXPRS.containsKey(key)) {
                EXPRS.put(key, new ArrayList<>());
            }
            EXPRS.get(key).add(expr);
        }
    };

    private final Deque<BasicExpr<AbstractInsnNode>> expressions;

    private ClassMethod method;

    /**
     * Constructs an ExprTree based on the given expressions.
     *
     * @param expressions The expressions to build an ExprTree for.
     */
    public ExprTree(Deque<BasicExpr<AbstractInsnNode>> expressions) {
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

    private static void handleExpr(Iterator<BasicExpr<AbstractInsnNode>> itr, BasicExpr<AbstractInsnNode> expr,
                                   BasicExpr<AbstractInsnNode> parent, String indent) {
        if (parent != null) {
            parent.addChild(expr);
        }
        if (Boolean.parseBoolean(System.getProperty(VERBOSE_EXPRESSION_TREE))) {
            System.out.println(indent + Assembly.toString(expr.insn()) + " (" + expr.proceeding() + ")");
        }
        // the amount of instructions that need to be 'popped'
        int proceeding = expr.proceeding();
        BasicExpr<AbstractInsnNode> prev = null;
        for (int i = 0; i < proceeding; i++) {
            BasicExpr<AbstractInsnNode> child = itr.next();
            if (prev != null) {
                // setRight/setLeft instead of setLeft/setRight due to Collections#reverse in #build
                child.setRight(prev);
                prev.setLeft(child);
            }
            // recursively handle children
            handleExpr(itr, child, expr, indent + "  ");
            prev = child;
            // determine the amount of instructions to skip
            i += consume(expr, child.insn());
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
        boolean verbose = Boolean.parseBoolean(System.getProperty(VERBOSE_EXPRESSION_TREE));
        if (verbose) {
            System.out.println("Building " + method.key() + "......");
        }
        Optional<ExprTree> treeOpt = build(EXPRS.get(method.key()));
        treeOpt.ifPresent(tree -> tree.method = method);
        if (verbose) {
            System.out.println();
        }
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
    private static Optional<ExprTree> build(List<BasicExpr<AbstractInsnNode>> exprList) {
        if (exprList == null) {
            return Optional.empty();
        }
        // Reverse the list to handle instructions by 'stack' order.
        Collections.reverse(exprList);
        Iterator<BasicExpr<AbstractInsnNode>> itr = exprList.iterator();
        Deque<BasicExpr<AbstractInsnNode>> exprs = new ArrayDeque<>();
        BasicExpr prev = null;
        while (itr.hasNext()) {
            BasicExpr<AbstractInsnNode> expr = itr.next();
            if (prev != null) {
                // setRight/setLeft instead of setLeft/setRight due to Collections#reverse
                expr.setRight(prev);
                prev.setLeft(expr);
            }
            // recursively handle expressions, so if the next needs to pop more instructions off,
            // it can do so while handling heritage correctly.
            handleExpr(itr, expr, null, "");
            // add to the front of the Deque to accommodate for calling Collections#reverse
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
    private static int consume(BasicExpr parent, AbstractInsnNode insn) {
        int op = insn.getOpcode();
        // longs and doubles take up two slots on the stack for consts + fields
        // ^ these should *only* compute if its parent is a MethodExpr
        if (parent instanceof MethodExpr) {
            if (op == LCONST_0 || op == LCONST_1 || op == DCONST_0 || op == DCONST_1 || op == I2L ||
                    op == F2L || op == D2L || op == L2D || op == F2D || op == I2D) {
                return 1;
            } else if (op == GETFIELD || op == GETSTATIC) {
                FieldInsnNode fin = (FieldInsnNode) insn;
                if (fin.desc.equals("J") || fin.desc.equals("D")) {
                    return 1;
                }
            }
        }
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
