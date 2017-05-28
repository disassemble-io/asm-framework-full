package io.disassemble.asm.visitor.expr;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.util.DupSizeCalculator;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import jdk.internal.org.objectweb.asm.util.Printer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tyler Sedlar
 * @since 5/28/2017
 */
public class ExprTreeBuilder {

    /**
     * Builds an ExprTree for the given method.
     *
     * @param method The method to build an ExprTree for.
     * @return An ExprTree for the given method.
     */
    public static Optional<ExprTree> build(ClassMethod method) {
        if (method == null) {
            return Optional.empty();
        }
        List<AbstractInsnNode> stack = new ArrayList<>();
        Collections.addAll(stack, method.instructions().toArray());
        Collections.reverse(stack);
        List<BasicExpr> exprList = new ArrayList<>();
        AtomicInteger stackIdx = new AtomicInteger(0);
        stack.forEach(insn -> exprList.add(new BasicExpr(method, insn, stackIdx.getAndIncrement(), resolveSize(insn))));
        Deque<BasicExpr> exprs = new ArrayDeque<>();
        AtomicInteger idx = new AtomicInteger(0);
        BasicExpr prev = null;
        while (idx.get() < exprList.size()) {
            int startIdx = idx.get();
            handleExpression(exprList, -1, idx, "");
            idx.incrementAndGet();
            BasicExpr expr = exprList.get(startIdx);
            if (prev != null) {
                expr.setRight(prev);
                prev.setLeft(expr);
            }
            exprs.add(expr);
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
                try {
                    build(method).ifPresent(factoryTrees::add);
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalStateException("Failed to parse tree @ " + method.key());
                }
            });
            trees.put(factory.name(), factoryTrees);
        });
        return trees;
    }

    /**
     * Handles the expression tree building process.
     *
     * @param exprList  The list of all expressions.
     * @param parentIdx The index of the parent expression.
     * @param idx       The index of the expression being used.
     * @param indent    The pre-filled text used for debugging.
     * @return The amount of instructions to consume (take place), if any slots need to be filled.
     */
    private static int handleExpression(List<BasicExpr> exprList, int parentIdx, AtomicInteger idx, String indent) {
        BasicExpr expr = idx.get() >= exprList.size() ? null : exprList.get(idx.get());
        int consume = 0;
        if (expr != null) {
            if (parentIdx != -1) {
                exprList.get(parentIdx).addChild(expr);
            }
            if (expr.opcode() == GOTO) {
                if (expr.parent() != null) {
//                    System.out.println(indent + Assembly.toString(expr.insn()) + " (" + expr.parent().size + ")");
                    consume =  expr.parent().size; // TODO: make this count the amount of instructions in the GOTO block.
                }
            } else {
                if (expr.opcode() == POP2) {
                    consume = 1;
                } else if (expr.opcode() == DUP_X1) {
                    consume = 1;
                } else if (expr.opcode() == DUP2 || expr.opcode() == DUP_X2 || expr.opcode() == DUP2_X1 || expr.opcode() == DUP2_X2) {
                    boolean dorl = isDoubleOrLong(exprList.get(idx.get() + 1).insn());
                    consume = (dorl ? 1 : 2);
                }
//                System.out.println(indent + Assembly.toString(expr.insn()) + " (" + expr.size + ")");
                BasicExpr prev = null;
                for (int i = 0; i < expr.size; i++) {
                    idx.incrementAndGet();
                    BasicExpr child = exprList.size() > idx.get() ? exprList.get(idx.get()) : null;
                    if (child != null && prev != null) {
                        // setRight/setLeft instead of setLeft/setRight due to Collections#reverse in #build
                        child.setRight(prev);
                        prev.setLeft(child);
                    }
                    i += handleExpression(exprList, expr.index, idx, indent + "  ");
                    prev = child;
                }
            }
        } else {
            String msg = "Unable to fetch expr @ " + idx.get() + " in " + exprList.get(0).method.key();
//            System.err.println(msg);
            throw new ArrayIndexOutOfBoundsException(msg);
        }
        return consume;
    }

    public static boolean isDoubleOrLong(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op == LCONST_0 || op == LCONST_1 || op == DCONST_0 || op == DCONST_1 ||
            op == I2L || op == F2L || op == D2L || op == L2D || op == F2D || op == I2D ||
            op == LADD || op == LSUB || op == LMUL || op == LDIV ||
            op == DADD || op == DSUB || op == DMUL || op == DDIV ||
            op == LOR || op == LAND || op == LREM || op == LNEG ||
            op == LSHL || op == LSHR || op == LLOAD || op == DLOAD ||
            op == LSTORE || op == DSTORE) {
            return true;
        } else if (op == GETFIELD || op == GETSTATIC) {
            FieldInsnNode fin = (FieldInsnNode) insn;
            if (fin.desc.equals("J") || fin.desc.equals("D")) {
                return true;
            }
        } else if (op == INVOKESTATIC || op == INVOKEVIRTUAL || op == INVOKEDYNAMIC) {
            MethodInsnNode min = (MethodInsnNode) insn;
            if (min.desc.endsWith(")J") || min.desc.endsWith(")D")) {
                return true;
            }
        } else if (op == LDC) {
            LdcInsnNode ldc = (LdcInsnNode) insn;
            if (ldc.cst != null && (ldc.cst instanceof Long || ldc.cst instanceof Double)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the amount of slots taken up by the given instruction.
     *
     * @param insn The instruction to find a size for.
     * @return The amount of slots taken up by the given instruction.
     */
    public static int resolveSize(AbstractInsnNode insn) {
        switch (insn.getOpcode()) {
            case NOP: {
                return 0;
            }
            case ACONST_NULL:
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case LCONST_0:
            case LCONST_1:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case DCONST_0:
            case DCONST_1:
            case BIPUSH:
            case SIPUSH:
            case LDC: {
                return 0;
            }
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD: {
                return 0;
            }
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD: {
                return 2;
            }
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE: {
                return 1;
            }
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE: {
                return 3;
            }
            case POP: {
                return 1;
            }
            case POP2: {
                if (isDoubleOrLong(insn.getPrevious())) {
                    return 1;
                }
                return 2;
            }
            case DUP: {
                return 1;
            }
            case DUP_X1: {
                return 2;
            }
            case DUP_X2: {
                return DupSizeCalculator.dup_x2(insn);
            }
            case DUP2: {
                return DupSizeCalculator.dup2(insn);
            }
            case DUP2_X1: {
                return DupSizeCalculator.dup2_x1(insn);
            }
            case DUP2_X2: {
                return DupSizeCalculator.dup2_x2(insn);
            }
            case SWAP: {
                return 2;
            }
            case IADD:
            case LADD:
            case FADD:
            case DADD:
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
            case IREM:
            case LREM:
            case FREM:
            case DREM: {
                return 2;
            }
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG: {
                return 1;
            }
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR: {
                return 2;
            }
            case IINC: {
                return 1;
            }
            case I2L:
            case I2F:
            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2B:
            case I2C:
            case I2S: {
                return 1;
            }
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG: {
                return 2;
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE: {
                return 1;
            }
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE: {
                return 2;
            }
            case GOTO: {
                return 0;
            }
            case JSR: {
                return 0;
            }
            case RET: {
                return 0;
            }
            case TABLESWITCH:
            case LOOKUPSWITCH: {
                return 1;
            }
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN: {
                return 1;
            }
            case RETURN: {
                return 0;
            }
            case GETSTATIC: {
                return 0;
            }
            case PUTSTATIC: {
                return 1;
            }
            case GETFIELD: {
                return 1;
            }
            case PUTFIELD: {
                return 2;
            }
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC: {
                String desc = ((MethodInsnNode) insn).desc;
                int count = 0;
                for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
                    count++;
                }
                if (insn.getOpcode() != INVOKESTATIC) {
                    count++;
                }
                return count;
            }
            case NEW: {
                return 0;
            }
            case NEWARRAY:
            case ANEWARRAY:
            case ARRAYLENGTH: {
                return 1;
            }
            case ATHROW: {
                return 1;
            }
            case CHECKCAST:
            case INSTANCEOF: {
                return 1;
            }
            case MONITORENTER:
            case MONITOREXIT: {
                return 1;
            }
            case MULTIANEWARRAY: {
                return ((MultiANewArrayInsnNode) insn).dims;
            }
            case IFNULL:
            case IFNONNULL: {
                return 1;
            }
            default: {
                if (insn instanceof LabelNode) {
                    return 0;
                } else if (insn instanceof LineNumberNode) {
                    return 0;
                }
                throw new RuntimeException("Illegal opcode " + insn.getOpcode() + " - " + insn);
            }
        }
    }
}
