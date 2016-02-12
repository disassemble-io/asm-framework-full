package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionPath {

    private final List<ExecutionNode> nodes = new ArrayList<>();
    protected final Map<String, ExecutionNode> idMap = new HashMap<>();

    private void add(ControlFlowGraph cfg, ExecutionNode eNode) {
        nodes.add(eNode);
        idMap.put(cfg.idFor(eNode.source), eNode);
    }

    private ExecutionNode findById(String id) {
        return idMap.get(id);
    }

    private static void addSuccessors(ControlFlowGraph cfg, ExecutionPath path, ExecutionNode eNode,
                                      ExecutionNode parent, List<String> added) {
        String nodeId = cfg.idFor(eNode.source);
        if (!added.contains(nodeId)) {
            added.add(nodeId);
            eNode.source.successors.forEach(subNode -> {
                ExecutionNode eSubNode = new ExecutionNode(path, subNode);
                parent.add(cfg, eSubNode);
                addSuccessors(cfg, path, eSubNode, (subNode.successors.size() > 1 ? eSubNode : parent), added);
            });
            parent.branch();
        }
    }

    private void print(String prefix, ExecutionNode node) {
        if (!(node.source.instruction instanceof LabelNode || node.source.instruction instanceof FrameNode)) {
            String label = (prefix + Assembly.toString(node.source.instruction));
            boolean layered = !node.paths().isEmpty();
            if (layered) {
                label += " {";
            }
            System.out.println(label);
            for (List<ExecutionNode> path : node.paths()) {
                System.out.println(prefix + "  {");
                for (ExecutionNode subNode : path) {
                    print(prefix + "    ", subNode);
                }
                System.out.println(prefix + "  }");
            }
            if (layered) {
                System.out.println(prefix + "}");
            }
        }
    }

    public void printTree() {
        for (ExecutionNode node : nodes) {
            print("", node);
        }
    }

    public static ExecutionPath build(ControlFlowGraph cfg) {
        ExecutionPath path = new ExecutionPath();
        AbstractInsnNode insn = cfg.method.instructions().getFirst();
        List<String> added = new ArrayList<>();
        while (insn != null) {
            ControlFlowNode node = cfg.nodeFor(insn);
            if (node != null) {
                String nodeId = cfg.idFor(node);
                if (path.findById(nodeId) == null) {
                    ExecutionNode eNode = new ExecutionNode(path, node);
                    if (eNode.source.successors.size() > 1) {
                        addSuccessors(cfg, path, eNode, eNode, added);
                    }
                    path.add(cfg, eNode);
                }
            }
            insn = insn.getNext();
        }
        return path;
    }
}
