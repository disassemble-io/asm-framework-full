package me.sedlar.asm.visitor.flow;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionNode {

    public final ExecutionPath path;
    public final ExecutionNode parent;
    public final ControlFlowNode source;
    public final String id;
    private final List<List<ExecutionNode>> paths = new ArrayList<>();
    protected ExecutionNode previousExecutor, previousNode, nextNode;
    private List<ExecutionNode> current = new ArrayList<>();

    public ExecutionNode(ExecutionPath path, ExecutionNode parent, ControlFlowNode source) {
        this.path = path;
        this.parent = parent;
        this.source = source;
        this.id = source.id;
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
     * Gets the path that branches to a true value.
     *
     * @return The path that branches to a true value.
     */
    public Optional<List<ExecutionNode>> truePath() {
        if (paths.size() == 1) {
            return Optional.of(paths.get(0));
        }
        if (source.instruction instanceof JumpInsnNode) {
            LabelNode label = ((JumpInsnNode) source.instruction).label;
            for (List<ExecutionNode> path : paths()) {
                if (label == path.get(0).source.instruction) {
                    return Optional.of(path);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the path that branches to a false value.
     *
     * @return The path that branches to a false value.
     */
    public Optional<List<ExecutionNode>> falsePath() {
        if (source.instruction instanceof JumpInsnNode) {
            LabelNode label = ((JumpInsnNode) source.instruction).label;
            for (List<ExecutionNode> path : paths()) {
                if (label != path.get(0).source.instruction) {
                    return Optional.of(path);
                }
            }
        }
        return Optional.empty();
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
        path.idMap.put(eNode.id, eNode);
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
