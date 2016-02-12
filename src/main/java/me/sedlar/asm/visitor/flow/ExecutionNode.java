package me.sedlar.asm.visitor.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionNode {

    private final List<List<ExecutionNode>> paths = new ArrayList<>();
    public final ExecutionPath parent;
    public final ControlFlowNode source;

    private List<ExecutionNode> current = new ArrayList<>();

    public ExecutionNode(ExecutionPath parent, ControlFlowNode source) {
        this.parent = parent;
        this.source = source;
    }

    public List<List<ExecutionNode>> paths() {
        return paths;
    }

    protected void add(ControlFlowGraph cfg, ExecutionNode eNode) {
        current.add(eNode);
        parent.idMap.put(cfg.idFor(eNode.source), eNode);
    }

    public void branch() {
        if (!current.isEmpty()) {
            paths.add(new ArrayList<>(current));
            current.clear();
        }
    }
}
