package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionPath {

    private final List<ExecutionNode> nodes = new ArrayList<>();
    protected final Map<String, ExecutionNode> idMap = new HashMap<>();

    public List<FlowQueryResult> query(FlowQuery query) {
        List<FlowQueryResult> results = new ArrayList<>();
        List<Predicate<ExecutionNode>> predicates = query.predicates();
        if (predicates.isEmpty()) {
            return results;
        }
        List<ExecutionNode> searching = nodes;
        List<ExecutionNode> lastMatch = null;
        boolean branching = false;
        List<ExecutionNode> endings = new ArrayList<>();
        for (int i = 0; i < predicates.size(); i++) {
            Predicate<ExecutionNode> predicate = predicates.get(i);
            List<ExecutionNode> matching;
            if (branching) {
                List<ExecutionNode> branchInstructions = new ArrayList<>();
                for (ExecutionNode node : lastMatch) {
                    node.paths().forEach(nodeList -> {
                        branchInstructions.addAll(nodeList);
                        nodeList.forEach(eNode -> eNode.previousExecutor = node);
                    });
                }
                searching = branchInstructions;
                matching = searching.stream().filter(predicate::test).collect(Collectors.toList());
            } else {
                if (i == 0) {
                    matching = nodes.stream().filter(predicate::test).collect(Collectors.toList());
                } else {
                    matching = new ArrayList<>();
                    for (ExecutionNode node : lastMatch) {
                        ExecutionNode result = findNext(searching.indexOf(node) + 1, searching, predicate,
                                query.distAt(i));
                        if (result != null) {
                            result.previousExecutor = node;
                            matching.add(result);
                        }
                    }
                }
            }
            if (matching.isEmpty()) {
                return results;
            }
            if (i == (predicates.size() - 1)) {
                endings.addAll(matching);
            }
            lastMatch = matching;
            branching = query.branchesAt(i);
        }
        for (ExecutionNode node : endings) {
            List<ExecutionNode> hierarchy = new ArrayList<>();
            hierarchy.add(node);
            while (node.previousExecutor != null) {
                hierarchy.add(node.previousExecutor);
                node = node.previousExecutor;
            }
            Collections.reverse(hierarchy);
            results.add(new FlowQueryResult(query, hierarchy));
        }
        return results;
    }

    private ExecutionNode findNext(int startIndex, List<ExecutionNode> searching,
                                   Predicate<ExecutionNode> predicate, int maxDist) {
        for (int i = startIndex; i < searching.size() && i < (startIndex + maxDist); i++) {
            ExecutionNode node = searching.get(i);
            if (predicate.test(node)) {
                return node;
            }
        }
        return null;
    }

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
                ExecutionNode eSubNode = new ExecutionNode(path, parent, subNode);
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
                    ExecutionNode eNode = new ExecutionNode(path, null, node);
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
