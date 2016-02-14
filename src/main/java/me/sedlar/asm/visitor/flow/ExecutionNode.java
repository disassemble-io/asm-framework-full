package me.sedlar.asm.visitor.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionNode {

    private final List<List<ExecutionNode>> paths = new ArrayList<>();
    public final ExecutionPath path;
    public final ExecutionNode parent;
    public final ControlFlowNode source;

    private List<ExecutionNode> current = new ArrayList<>();

    protected ExecutionNode previousExecutor, previousNode, nextNode;

    public ExecutionNode(ExecutionPath path, ExecutionNode parent, ControlFlowNode source) {
        this.path = path;
        this.parent = parent;
        this.source = source;
    }

    /**
     * Gets a list of paths this node can execute.
     *
     * @return A list of paths this node can execute.
     */
    public List<List<ExecutionNode>> paths() {
        return paths;
    }

    /**
     * Gets the ExecutionNode prior to this node.
     *
     * @return The ExecutionNode prior to this node.
     */
    public ExecutionNode previous() {
        return previousNode;
    }

    /**
     * Gets the ExecutionNode following this node.
     *
     * @return The ExecutionNode following this node.
     */
    public ExecutionNode next() {
        return nextNode;
    }

    protected void add(ControlFlowGraph cfg, ExecutionNode eNode) {
        current.add(eNode);
        path.idMap.put(cfg.idFor(eNode.source), eNode);
    }

    /**
     * Creates a branch in paths.
     */
    public void branch() {
        if (!current.isEmpty()) {
            paths.add(new ArrayList<>(current));
            current.clear();
        }
    }
}
