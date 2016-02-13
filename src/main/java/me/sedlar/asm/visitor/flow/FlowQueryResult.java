package me.sedlar.asm.visitor.flow;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 2/13/2016
 */
public class FlowQueryResult {

    private final FlowQuery query;
    private final List<ExecutionNode> nodes;

    public FlowQueryResult(FlowQuery query, List<ExecutionNode> nodes) {
        this.query = query;
        this.nodes = nodes;
    }

    /**
     * Finds the ExecutionNode with the given name.
     *
     * @param name The name to search for.
     * @return The ExecutionNode with the given name.
     */
    public Optional<ExecutionNode> findExecutor(String name) {
        for (int i = 0; i < nodes.size(); i++) {
            String nodeName = query.nameAt(i);
            if (nodeName != null && nodeName.equals(name)) {
                return Optional.ofNullable(nodes.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the ControlFlowNode with the given name.
     *
     * @param name The name to search for.
     * @return The ControlFlowNode with the given name.
     */
    public Optional<ControlFlowNode> findNode(String name) {
        Optional<ExecutionNode> executor = findExecutor(name);
        return (executor.isPresent() ? Optional.ofNullable(executor.get().source) : Optional.empty());
    }

    /**
     * Finds the AbstractInsnNode with the given name.
     *
     * @param name The name to search for.
     * @return The AbstractInsnNode with the given name.
     */
    public Optional<AbstractInsnNode> findInstruction(String name) {
        Optional<ControlFlowNode> node = findNode(name);
        return (node.isPresent() ? Optional.ofNullable(node.get().instruction) : Optional.empty());
    }
}
