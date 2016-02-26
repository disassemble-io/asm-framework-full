package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class ExecutionPath {

    private static final Predicate<ExecutionNode> LABEL_PRED = (e) -> e.source.instruction instanceof LabelNode;
    private static final Predicate<ExecutionNode> JUMP_PRED = (e) -> e.source.instruction instanceof JumpInsnNode;

    private final List<ExecutionNode> nodes = new ArrayList<>();
    protected final Map<String, ExecutionNode> idMap = new HashMap<>();

    private void findAll(ExecutionNode parent, Predicate<ExecutionNode> predicate,
                         List<ExecutionNode> list, boolean recursive) {
        if (predicate.test(parent)) {
            list.add(parent);
        }
        for (List<ExecutionNode> path : parent.paths()) {
            for (ExecutionNode node : path) {
                if (recursive) {
                    findAll(node, predicate, list, true);
                } else {
                    if (predicate.test(node)) {
                        list.add(node);
                    }
                }
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
            findAll(node, predicate, result, true);
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
        FlowQuery.BranchType branchType = null;
        List<ExecutionNode> endings = new ArrayList<>();
        for (int i = 0; i < predicates.size(); i++) {
            Predicate<ExecutionNode> predicate = predicates.get(i);
            List<ExecutionNode> matching;
            if (branching) {
                List<ExecutionNode> branchInstructions = new ArrayList<>();
                for (ExecutionNode node : lastMatch) {
                    Consumer<List<ExecutionNode>> consumer = (nodeList -> {
                        branchInstructions.addAll(nodeList);
                        nodeList.forEach(eNode -> eNode.previousExecutor = node);
                    });
                    if (branchType == FlowQuery.BranchType.TRUE) {
                        node.truePath().ifPresent(consumer);
                    } else if (branchType == FlowQuery.BranchType.FALSE) {
                        node.falsePath().ifPresent(consumer);
                    } else {
                        node.paths().forEach(consumer);
                    }
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
                            boolean loops = query.loopsAt(i);
                            boolean doesNotLoop = query.doesNotLoopAt(i);
                            if (loops || doesNotLoop) {
                                ExecutionNode parent = node.parent;
                                ExecutionNode parentLabel = null;
                                while (parent != null && (parentLabel = findPrevious(parent, LABEL_PRED, 5)) == null) {
                                    parent = parent.parent;
                                }
                                if (parentLabel == null) {
                                    continue;
                                }
                                boolean valid = false;
                                List<ExecutionNode> jumps = new ArrayList<>();
                                findAll(node.parent, JUMP_PRED, jumps, false);
                                if (!jumps.isEmpty()) {
                                    for (ExecutionNode jump : jumps) {
                                        LabelNode label = ((JumpInsnNode) jump.source.instruction).label;
                                        if (label == parentLabel.source.instruction) {
                                            valid = true;
                                        }
                                    }
                                }
                                if (!valid) {
                                    if (doesNotLoop) {
                                        result.previousExecutor = node;
                                        matching.add(result);
                                    }
                                    continue;
                                }
                            }
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
            branchType = query.branchTypeAt(i);
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
        while ((node = node.next()) != null && (jump == -1 || jump++ < maxDist)) {
            if (predicate.test(node)) {
                return node;
            }
        }
        return null;
    }

    private ExecutionNode findPrevious(ExecutionNode start, Predicate<ExecutionNode> predicate, int maxDist) {
        ExecutionNode node = start;
        int jump = 0;
        while ((node = node.previous()) != null && (jump == -1 || jump++ < maxDist)) {
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

    public void printTree() {
        for (ExecutionNode node : nodes) {
            print("", node);
        }
    }

    private static void addSuccessors(ControlFlowGraph cfg, ExecutionPath path, ExecutionNode eNode,
                                      ExecutionNode parent, List<String> added,
                                      List<ExecutionNode> successors) {
        if (eNode.source.backwards) {
            return;
        }
        String nodeId = cfg.idFor(eNode.source);
        if (!added.contains(nodeId)) {
            added.add(nodeId);
            path.idMap.put(nodeId, eNode);
            eNode.source.successors.forEach(subNode -> {
                ExecutionNode eSubNode = new ExecutionNode(path, parent, subNode);
                parent.add(cfg, eSubNode);
                successors.add(eSubNode);
                addSuccessors(cfg, path, eSubNode, (subNode.successors.size() > 1 ? eSubNode : parent),
                        added, successors);
            });
            parent.branch();
        }
    }

    private static void setNextNodes(List<ExecutionNode> nodes) {
        ExecutionNode previous = null;
        for (ExecutionNode node : nodes) {
            if (previous != null) {
                previous.nextNode = node;
                node.previousNode = previous;
            }
            node.paths().forEach(ExecutionPath::setNextNodes);
            previous = node;
        }
    }

    public static ExecutionPath build(ControlFlowGraph cfg) {
        ExecutionPath path = new ExecutionPath();
        AbstractInsnNode insn = cfg.method.instructions().getFirst();
        List<String> added = new ArrayList<>();
        while (insn != null) {
            ControlFlowNode node = cfg.nodeFor(insn, false);
            if (node != null) {
                String nodeId = cfg.idFor(node);
                if (path.findById(nodeId) == null) {
                    ExecutionNode eNode = new ExecutionNode(path, null, node);
                    if (eNode.source.successors.size() > 1) {
                        List<ExecutionNode> successors = new ArrayList<>();
                        addSuccessors(cfg, path, eNode, eNode, added, successors);
                    }
                    path.add(cfg, eNode);
                }
            }
            insn = insn.getNext();
        }
        setNextNodes(path.nodes);
        return path;
    }
}
