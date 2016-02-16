package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionPath {

    protected final Map<String, ExecutionNode> idMap = new HashMap<>();
    private final List<ExecutionNode> nodes = new ArrayList<>();

    private static void addSuccessors(ControlFlowGraph cfg, ExecutionPath path, ExecutionNode eNode,
                                      ExecutionNode parent, Set<String> added) {
        if (eNode.source.backwards || (parent != null && parent.source.backwards)) {
            return;
        }
        eNode.source.successors.forEach(subNode -> {
            if (!added.contains(subNode.id) && !subNode.backwards) {
                added.add(subNode.id);
                ExecutionNode eSubNode = new ExecutionNode(path, parent, subNode);
                if (parent != null) {
                    parent.add(cfg, eSubNode);
                } else {
                    path.add(cfg, eSubNode);
                }
                addSuccessors(cfg, path, eSubNode, (subNode.successors.size() > 1 ? eSubNode : parent), added);
            }
        });
        if (parent != null) {
            parent.branch();
        }
    }

    private static void setNextNodes(List<ExecutionNode> nodes) {
        ExecutionNode previous = null;
        for (ExecutionNode node : nodes) {
            if (previous != null) {
                previous.nextNode = node;
            }
            node.paths().forEach(ExecutionPath::setNextNodes);
            previous = node;
        }
    }

    /**
     * Builds an ExecutionPath for the given ControlFlowGraph.
     *
     * @param cfg The ControlFlowGraph to build an ExecutionPath for.
     * @return An ExecutionPath for the given ControlFlowGraph.
     */
    public static ExecutionPath build(ControlFlowGraph cfg) {
        ExecutionPath path = new ExecutionPath();
        AbstractInsnNode insn = cfg.method.instructions().getFirst();
        Set<String> added = new HashSet<>();
        while (insn != null) {
            ControlFlowNode node = cfg.nodeFor(insn, false);
            if (node != null) {
                String nodeId = cfg.idFor(node);
                if (path.findById(nodeId) == null) {
                    ExecutionNode eNode = new ExecutionNode(path, null, node);
                    path.add(cfg, eNode);
                    ExecutionNode parent = (node.successors.size() > 1 ? eNode : null);
                    added.add(nodeId);
                    addSuccessors(cfg, path, eNode, parent, added);
                }
            }
            insn = insn.getNext();
        }
        setNextNodes(path.nodes);
        return path;
    }

    private void findAll(ExecutionNode parent, Predicate<ExecutionNode> predicate, List<ExecutionNode> list) {
        if (predicate.test(parent)) {
            list.add(parent);
        }
        for (List<ExecutionNode> path : parent.paths()) {
            for (ExecutionNode node : path) {
                findAll(node, predicate, list);
            }
        }
    }

    /**
     * Finds all nodes matching the given predicate.
     *
     * @param predicate The predicate to match against.
     * @return A list of all nodes matching the given predicate.
     */
    public List<ExecutionNode> findAll(Predicate<ExecutionNode> predicate) {
        List<ExecutionNode> result = new ArrayList<>();
        for (ExecutionNode node : nodes) {
            findAll(node, predicate, result);
        }
        return result;
    }

    /**
     * Finds a list of results matching the given query.
     *
     * @param query The query to match.
     * @return A list of results matching the given query.
     */
    public List<FlowQueryResult> query(FlowQuery query) {
        List<FlowQueryResult> results = new ArrayList<>();
        List<Predicate<ExecutionNode>> predicates = query.predicates();
        if (predicates.isEmpty()) {
            return results;
        }
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
                matching = branchInstructions.stream().filter(predicate::test).collect(Collectors.toList());
            } else {
                if (i == 0) {
                    matching = findAll(predicate);
                } else {
                    matching = new ArrayList<>();
                    for (ExecutionNode node : lastMatch) {
                        ExecutionNode result = findNext(node, predicate, query.distAt(i));
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

    private ExecutionNode findNext(ExecutionNode start, Predicate<ExecutionNode> predicate, int maxDist) {
        ExecutionNode node = start;
        int jump = 0;
        while ((node = node.nextNode) != null && (jump == -1 || jump++ < maxDist)) {
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

    protected ExecutionNode findById(String id) {
        return idMap.get(id);
    }

    private void print(String prefix, ExecutionNode node) {
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

    /**
     * Prints out the paths as a tree-like structure.
     */
    public void printTree() {
        for (ExecutionNode node : nodes) {
            print("", node);
        }
    }
}
