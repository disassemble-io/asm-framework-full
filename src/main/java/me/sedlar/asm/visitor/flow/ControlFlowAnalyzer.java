package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.visitor.flow.lambdamix.SimpleFramelessAnalyzer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public class ControlFlowAnalyzer extends SimpleFramelessAnalyzer {

    protected ControlFlowGraph graph;
    protected InsnList instructions;

    @Override
    protected void newControlFlowEdge(int insn, int successor) {
        AbstractInsnNode from = instructions.get(insn);
        AbstractInsnNode to = instructions.get(successor);
        boolean backwards = (successor < insn);
        graph.add(from, to, backwards);
    }

    @Override
    protected boolean newControlFlowExceptionEdge(int insn, int successor) {
        AbstractInsnNode from = instructions.get(insn);
        AbstractInsnNode to = instructions.get(successor);
        boolean backwards = (successor < insn);
        graph.exception(from, to, backwards);
        return super.newControlFlowExceptionEdge(insn, successor);
    }
}
