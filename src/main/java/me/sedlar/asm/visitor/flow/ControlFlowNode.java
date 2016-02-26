package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author The Android Open Source Project, Tyler Sedlar
 * @since 2/12/2016
 * <p>
 * A {@link ControlFlowNode} is a node in the control flow graph for a method, pointing to
 * the instruction and its possible successors
 */
public class ControlFlowNode {

    /**
     * The graph
     */
    public final ControlFlowGraph graph;

    /**
     * The instruction
     */
    public final AbstractInsnNode instruction;

    /**
     * Any normal successors (e.g. following instruction, or goto or conditional flow)
     */
    public final List<ControlFlowNode> successors = new ArrayList<>();

    public final List<ControlFlowNode> predecessors = new ArrayList<>();

    /**
     * Any abnormal successors (e.g. the handler to go to following an exception)
     */
    public final List<ControlFlowNode> exceptions = new ArrayList<>();
    public boolean backwards;
    protected String id;

    /**
     * Constructs a new control graph node
     *
     * @param instruction the instruction to associate with this node
     */
    public ControlFlowNode(ControlFlowGraph graph, AbstractInsnNode instruction, boolean backwards) {
        this.graph = graph;
        this.instruction = instruction;
        this.backwards = backwards;
    }

    protected void addSuccessor(ControlFlowNode node) {
        if (!successors.contains(node)) {
            successors.add(node);
        }
        if (!node.predecessors.contains(this)) {
            node.predecessors.add(this);
        }
    }

    protected void addExceptionPath(ControlFlowNode node) {
        if (!exceptions.contains(node)) {
            exceptions.add(node);
        }
    }

    /**
     * Converts this node to a .dot graph format.
     *
     * @param highlight A list of nodes to highlight.
     * @return This node in a .dot graph format.
     */
    public String toDot(Set<ControlFlowNode> highlight) {
        return graph.toDot(instruction, highlight);
    }

    /**
     * Represents this instruction as a string, for debugging purposes
     *
     * @param includeAdjacent whether it should include a display of
     *                        adjacent nodes as well
     * @return a string representation
     */
    public String toString(boolean includeAdjacent) {
        StringBuilder sb = new StringBuilder();
        sb.append(graph.idFor(instruction));
        sb.append(':');
        if (instruction instanceof LabelNode) {
            //LabelControlFlowNode l = (LabelControlFlowNode) instruction;
            //sb.append('L' + l.getLabel().getOffset() + ":");
            //sb.append('L' + l.getLabel().info + ":");
            sb.append("LABEL");
        } else if (instruction instanceof LineNumberNode) {
            sb.append("LINENUMBER ").append(((LineNumberNode) instruction).line);
        } else if (instruction instanceof FrameNode) {
            sb.append("FRAME");
        } else {
            int opcode = instruction.getOpcode();
            String opcodeName = Assembly.opname(opcode);
            sb.append(opcodeName);
            if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
                sb.append('(').append(((MethodInsnNode) instruction).name).append(')');
            }
        }
        if (includeAdjacent) {
            if (!successors.isEmpty()) {
                sb.append(" Next:");
                for (ControlFlowNode successor : successors) {
                    sb.append(' ');
                    sb.append(successor.toString(false));
                }
            }
            if (!exceptions.isEmpty()) {
                sb.append(" Exceptions:");
                for (ControlFlowNode exception : exceptions) {
                    sb.append(' ');
                    sb.append(exception.toString(false));
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
