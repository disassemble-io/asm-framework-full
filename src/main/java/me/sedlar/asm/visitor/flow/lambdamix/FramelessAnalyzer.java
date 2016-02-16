package me.sedlar.asm.visitor.flow.lambdamix;

import me.sedlar.asm.ClassMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lambdamix, Tyler Sedlar
 * @since 2/15/2016
 */
public class FramelessAnalyzer implements Opcodes {

    protected boolean[] wasQueued;
    protected boolean[] queued;
    protected int[] queue;
    protected int top;
    private int n;
    private InsnList insns;
    private List<TryCatchBlockNode>[] handlers;
    private Subroutine[] subroutines;

    @SuppressWarnings("unchecked")
    public void analyze(ClassMethod method) throws AnalyzerException {
        n = method.instructions().size();
        if ((method.access() & (ACC_ABSTRACT | ACC_NATIVE)) != 0 || n == 0) {
            return;
        }
        insns = method.instructions();
        handlers = (List<TryCatchBlockNode>[]) new List<?>[n];
        subroutines = new Subroutine[n];
        queued = new boolean[n];
        wasQueued = new boolean[n];
        queue = new int[n];
        top = 0;
        // computes exception handlers for each instruction
        for (int i = 0; i < method.method.tryCatchBlocks.size(); ++i) {
            TryCatchBlockNode tcb = (TryCatchBlockNode) method.method.tryCatchBlocks.get(i);
            int begin = insns.indexOf(tcb.start);
            int end = insns.indexOf(tcb.end);
            for (int j = begin; j < end; ++j) {
                List<TryCatchBlockNode> insnHandlers = handlers[j];
                if (insnHandlers == null) {
                    insnHandlers = new ArrayList<>();
                    handlers[j] = insnHandlers;
                }
                insnHandlers.add(tcb);
            }
        }
        // computes the subroutine for each instruction:
        Subroutine main = new Subroutine(null, method.method.maxLocals, null);
        List<AbstractInsnNode> subroutineCalls = new ArrayList<>();
        Map<LabelNode, Subroutine> subroutineHeads = new HashMap<>();
        findSubroutine(0, main, subroutineCalls);
        while (!subroutineCalls.isEmpty()) {
            JumpInsnNode jsr = (JumpInsnNode) subroutineCalls.remove(0);
            Subroutine sub = subroutineHeads.get(jsr.label);
            if (sub == null) {
                sub = new Subroutine(jsr.label, method.method.maxLocals, jsr);
                subroutineHeads.put(jsr.label, sub);
                findSubroutine(insns.indexOf(jsr.label), sub, subroutineCalls);
            } else {
                sub.callers.add(jsr);
            }
        }
        for (int i = 0; i < n; ++i) {
            if (subroutines[i] != null && subroutines[i].start == null) {
                subroutines[i] = null;
            }
        }
        merge(0, null);
        // control flow analysis
        while (top > 0) {
            int insn = queue[--top];
            Subroutine subroutine = subroutines[insn];
            queued[insn] = false;
            AbstractInsnNode insnNode = null;
            try {
                insnNode = method.instructions().get(insn);
                int insnOpcode = insnNode.getOpcode();
                int insnType = insnNode.getType();
                if (insnType == AbstractInsnNode.LABEL || insnType == AbstractInsnNode.LINE ||
                    insnType == AbstractInsnNode.FRAME) {
                    merge(insn + 1, subroutine);
                    newControlFlowEdge(insn, insn + 1);
                } else {
                    subroutine = subroutine == null ? null : subroutine.copy();
                    if (insnNode instanceof JumpInsnNode) {
                        JumpInsnNode j = (JumpInsnNode) insnNode;
                        if (insnOpcode != GOTO && insnOpcode != JSR) {
                            merge(insn + 1, subroutine);
                            newControlFlowEdge(insn, insn + 1);
                        }
                        int jump = insns.indexOf(j.label);
                        if (insnOpcode == JSR) {
                            merge(jump, new Subroutine(j.label, method.method.maxLocals, j));
                        } else {
                            merge(jump, subroutine);
                        }
                        newControlFlowEdge(insn, jump);
                    } else if (insnNode instanceof LookupSwitchInsnNode || insnNode instanceof TableSwitchInsnNode) {
                        int jump;
                        List labels;
                        if (insnNode instanceof LookupSwitchInsnNode) {
                            LookupSwitchInsnNode lsi = (LookupSwitchInsnNode) insnNode;
                            jump = insns.indexOf(lsi.dflt);
                            labels = lsi.labels;
                        } else {
                            TableSwitchInsnNode tsi = (TableSwitchInsnNode) insnNode;
                            jump = insns.indexOf(tsi.dflt);
                            labels = tsi.labels;
                        }
                        merge(jump, subroutine);
                        newControlFlowEdge(insn, jump);
                        for (Object label : labels) {
                            LabelNode labelNode = (LabelNode) label;
                            jump = insns.indexOf(labelNode);
                            merge(jump, subroutine);
                            newControlFlowEdge(insn, jump);
                        }
                    } else if (insnOpcode == RET) {
                        if (subroutine == null) {
                            throw new AnalyzerException(insnNode, "RET instruction outside of a sub routine");
                        }
                        for (int i = 0; i < subroutine.callers.size(); ++i) {
                            JumpInsnNode caller = subroutine.callers.get(i);
                            int call = insns.indexOf(caller);
                            if (wasQueued[call]) {
                                merge(call + 1, subroutines[call], subroutine.access);
                                newControlFlowEdge(insn, call + 1);
                            }
                        }
                    } else if (insnOpcode != ATHROW && (insnOpcode < IRETURN || insnOpcode > RETURN)) {
                        if (subroutine != null) {
                            if (insnNode instanceof VarInsnNode) {
                                int var = ((VarInsnNode) insnNode).var;
                                subroutine.access[var] = true;
                                if (insnOpcode == LLOAD || insnOpcode == DLOAD ||
                                    insnOpcode == LSTORE || insnOpcode == DSTORE) {
                                    subroutine.access[var + 1] = true;
                                }
                            } else if (insnNode instanceof IincInsnNode) {
                                int var = ((IincInsnNode) insnNode).var;
                                subroutine.access[var] = true;
                            }
                        }
                        merge(insn + 1, subroutine);
                        newControlFlowEdge(insn, insn + 1);
                    }
                }
                List<TryCatchBlockNode> insnHandlers = handlers[insn];
                if (insnHandlers != null) {
                    for (TryCatchBlockNode tcb : insnHandlers) {
                        newControlFlowExceptionEdge(insn, tcb);
                        merge(insns.indexOf(tcb.handler), subroutine);
                    }
                }
            } catch (AnalyzerException e) {
                throw new AnalyzerException(e.node, "Error at instruction " + insn + ": " + e.getMessage(), e);
            } catch (Exception e) {
                throw new AnalyzerException(insnNode, "Error at instruction " + insn + ": " + e.getMessage(), e);
            }
        }
    }

    protected void findSubroutine(int insn, Subroutine sub, List<AbstractInsnNode> calls) throws AnalyzerException {
        while (true) {
            if (insn < 0 || insn >= n) {
                throw new AnalyzerException(null, "Execution can fall off end of the code");
            }
            if (subroutines[insn] != null) {
                return;
            }
            subroutines[insn] = sub.copy();
            AbstractInsnNode node = insns.get(insn);
            // calls findSubroutine recursively on normal successors
            if (node instanceof JumpInsnNode) {
                if (node.getOpcode() == JSR) {
                    // do not follow a JSR, it leads to another subroutine!
                    calls.add(node);
                } else {
                    JumpInsnNode jnode = (JumpInsnNode) node;
                    findSubroutine(insns.indexOf(jnode.label), sub, calls);
                }
            } else if (node instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode tsnode = (TableSwitchInsnNode) node;
                findSubroutine(insns.indexOf(tsnode.dflt), sub, calls);
                for (int i = tsnode.labels.size() - 1; i >= 0; --i) {
                    LabelNode l = (LabelNode) tsnode.labels.get(i);
                    findSubroutine(insns.indexOf(l), sub, calls);
                }
            } else if (node instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode lsnode = (LookupSwitchInsnNode) node;
                findSubroutine(insns.indexOf(lsnode.dflt), sub, calls);
                for (int i = lsnode.labels.size() - 1; i >= 0; --i) {
                    LabelNode l = (LabelNode) lsnode.labels.get(i);
                    findSubroutine(insns.indexOf(l), sub, calls);
                }
            }
            // calls findSubroutine recursively on exception handler successors
            List<TryCatchBlockNode> insnHandlers = handlers[insn];
            if (insnHandlers != null) {
                for (TryCatchBlockNode tcb : insnHandlers) {
                    findSubroutine(insns.indexOf(tcb.handler), sub, calls);
                }
            }
            // if insn does not falls through to the next instruction, return.
            switch (node.getOpcode()) {
                case GOTO:
                case RET:
                case TABLESWITCH:
                case LOOKUPSWITCH:
                case IRETURN:
                case LRETURN:
                case FRETURN:
                case DRETURN:
                case ARETURN:
                case RETURN:
                case ATHROW: {
                    return;
                }
            }
            insn++;
        }
    }

    protected void newControlFlowEdge(int insn, int successor) {
    }

    protected boolean newControlFlowExceptionEdge(int insn, int successor) {
        return true;
    }

    protected boolean newControlFlowExceptionEdge(int insn, TryCatchBlockNode tcb) {
        return newControlFlowExceptionEdge(insn, insns.indexOf(tcb.handler));
    }

    protected void merge(int insn, Subroutine subroutine) throws AnalyzerException {
        Subroutine oldSubroutine = subroutines[insn];
        boolean changes = false;
        if (!wasQueued[insn]) {
            wasQueued[insn] = true;
            changes = true;
        }
        if (oldSubroutine == null) {
            if (subroutine != null) {
                subroutines[insn] = subroutine.copy();
                changes = true;
            }
        } else {
            if (subroutine != null) {
                changes |= oldSubroutine.merge(subroutine);
            }
        }
        if (changes && !queued[insn]) {
            queued[insn] = true;
            queue[top++] = insn;
        }
    }

    protected void merge(int insn, Subroutine subroutineBeforeJSR, boolean[] access) throws AnalyzerException {
        Subroutine oldSubroutine = subroutines[insn];
        boolean changes = false;
        if (!wasQueued[insn]) {
            wasQueued[insn] = true;
            changes = true;
        }
        if (oldSubroutine != null && subroutineBeforeJSR != null) {
            changes |= oldSubroutine.merge(subroutineBeforeJSR);
        }
        if (changes && !queued[insn]) {
            queued[insn] = true;
            queue[top++] = insn;
        }
    }
}