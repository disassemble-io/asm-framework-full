package io.disassemble.asm.visitor.flow;

import io.disassemble.asm.ClassMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 4/8/16
 */
public class ControlFlowGraph {

    final List<BasicBlock> blocks = new ArrayList<>();

    public final ClassMethod method;

    private ExecutionPath execution;

    public ControlFlowGraph(ClassMethod method) {
        this.method = method;
    }

    protected void flatten() {
        List<BasicBlock> merged = new ArrayList<>();
        blocks.sort((b1, b2) -> b1.start - b2.start);
        blocks.stream()
                .filter(block -> !merged.contains(block) && block.successors.size() == 1)
                .forEach(block -> {
                    merged.add(block);
                    BasicBlock successor = block.successors.get(0);
                    while (!merged.contains(successor)) {
                        block.append(successor);
                        block.successors.remove(successor);
                        merged.add(successor);
                        if (successor.successors.size() == 1) {
                            successor = successor.successors.get(0);
                        } else {
                            break;
                        }
                    }
                    merged.remove(block);
                });
        blocks.removeAll(merged);
    }

    /**
     * Obtains a list of this graph's BasicBlocks.
     *
     * @return A list of this graph's BasicBlocks.
     */
    public List<BasicBlock> blocks() {
        return Collections.unmodifiableList(blocks);
    }

    /**
     * Gets the execution path for the graph.
     *
     * @param cached Retrieve by cache, if the execution path has been built before.
     * @return The execution path for the graph.
     */
    public ExecutionPath execution(boolean cached) {
        if (cached && execution != null) {
            return execution;
        }
        return (execution = new ExecutionPath(blocks()));
    }

    /**
     * Gets the execution path for the graph.
     *
     * @return The execution path for the graph.
     */
    public ExecutionPath execution() {
        return execution(true);
    }

    /**
     * Creates a ControlFlowGraph for the given method.
     *
     * @param method The method to create a ControlFlowGraph for.
     * @return A ControlFlowGraph for the given method.
     */
    public static ControlFlowGraph create(ClassMethod method) {
        ControlFlowGraph graph = new ControlFlowGraph(method);
        FlowVisitor visitor = new FlowVisitor();
        visitor.setGraph(graph);
        method.accept(visitor);
        graph.flatten();
        return graph;
    }
}
