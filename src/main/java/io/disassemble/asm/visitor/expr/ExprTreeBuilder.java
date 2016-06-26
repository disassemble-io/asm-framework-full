package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import io.disassemble.asm.visitor.expr.node.MethodExpr;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/17/16
 */
public class ExprTreeBuilder {

    /**
     * Builds an ExprTree for the given method.
     *
     * @param method The method to build an ExprTree for.
     * @return An ExprTree for the given method.
     */
    @SuppressWarnings("unchecked")
    public static Optional<ExprTree> build(ClassMethod method) {
        if (method == null) {
            return Optional.empty();
        }
        List<AbstractInsnNode> stack = Arrays.asList(method.instructions().toArray());
        Collections.reverse(stack);
        Iterator<AbstractInsnNode> itr = stack.iterator();
        Deque<BasicExpr<AbstractInsnNode>> exprs = new ArrayDeque<>();
        BasicExpr<AbstractInsnNode> prev = null;
        while (itr.hasNext()) {
            AbstractInsnNode next = itr.next();
            BasicExpr<AbstractInsnNode> expr = BasicExpr.resolve(method, next, BasicExpr.resolveType(next));
            if (prev != null) {
                expr.setRight(prev);
                prev.setLeft(expr);
            }
            handleExpr(method, itr, expr, null, "");
            exprs.addFirst(expr);
            prev = expr;
        }
        return Optional.of(new ExprTree(method, exprs));
    }

    /**
     * Builds a map of ExprTrees for the given classes.
     *
     * @param classes The classes to build ExprTrees for.
     * @return A map of ExprTrees for the given classes.
     */
    public static Map<String, Deque<ExprTree>> buildAll(ConcurrentMap<String, ClassFactory> classes) {
        Map<String, Deque<ExprTree>> trees = new HashMap<>();
        classes.values().parallelStream().forEach(factory -> {
            Deque<ExprTree> factoryTrees = new ArrayDeque<>();
            Arrays.asList(factory.methods).parallelStream().forEach(method -> {
                build(method).ifPresent(factoryTrees::add);
            });
            trees.put(factory.name(), factoryTrees);
        });
        return trees;
    }

    @SuppressWarnings("unchecked")
    private static void handleExpr(ClassMethod method, Iterator<AbstractInsnNode> itr,
                                   BasicExpr<AbstractInsnNode> expr,
                                   BasicExpr<AbstractInsnNode> parent, String indent) {
        if (parent != null) {
            parent.addChild(expr);
        }
        // the amount of instructions that need to be 'popped'
        int proceeding = expr.proceeding();
        BasicExpr<AbstractInsnNode> prev = null;
        for (int i = 0; i < proceeding; i++) {
            AbstractInsnNode next = itr.next();
            BasicExpr<AbstractInsnNode> child = BasicExpr.resolve(method, next, BasicExpr.resolveType(next));
            if (prev != null) {
                // setRight/setLeft instead of setLeft/setRight due to Collections#reverse in #build
                child.setRight(prev);
                prev.setLeft(child);
            }
            // recursively handle children
            handleExpr(method, itr, child, expr, indent + "  ");
            prev = child;
            // determine the amount of instructions to skip
            i += consume(expr, child.insn());
        }
    }

    private static int consume(BasicExpr parent, AbstractInsnNode insn) {
        int op = insn.getOpcode();
        // longs and doubles take up two slots on the stack for consts + fields
        // ^ these should *only* compute if its parent is a MethodExpr
        if (parent instanceof MethodExpr) {
            if (op == LCONST_0 || op == LCONST_1 || op == DCONST_0 || op == DCONST_1 ||
                    op == I2L || op == F2L || op == D2L || op == L2D || op == F2D || op == I2D ||
                    op == LADD || op == LSUB || op == LMUL || op == LDIV ||
                    op == DADD || op == DSUB || op == DMUL || op == DDIV ||
                    op == LOR || op == LAND || op == LREM || op == LNEG ||
                    op == LSHL || op == LSHR || op == LLOAD || op == DLOAD ||
                    op == LSTORE || op == DSTORE) {
                return 1;
            } else if (op == GETFIELD || op == GETSTATIC) {
                FieldInsnNode fin = (FieldInsnNode) insn;
                if (fin.desc.equals("J") || fin.desc.equals("D")) {
                    return 1;
                }
            } else if (op == INVOKESTATIC || op == INVOKEVIRTUAL || op == INVOKEDYNAMIC) {
                MethodInsnNode min = (MethodInsnNode) insn;
                if (min.desc.endsWith(")J") || min.desc.endsWith(")D")) {
                    return 1;
                }
            } else if (op == LDC) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst != null && (ldc.cst instanceof Long || ldc.cst instanceof Double)) {
                    return 1;
                }
            }
        }
        if (op == DUP2) {
            return 1;
        } else if (op == DUP_X1) {
            return 2;
        } else if (op == DUP_X2 || op == DUP2_X1) {
            return 3;
        } else if (op == DUP2_X2) {
            return 4;
        }
        return 0;
    }
}
