package me.sedlar.asm.visitor.flow;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/26/2016
 */
public class ExecutionPath {

    private static final Predicate<BasicInstruction> LABEL_PRED = (b) -> b.insn instanceof LabelNode;
    private static final Predicate<BasicInstruction> JUMP_PRED = (b) -> b.insn instanceof JumpInsnNode;

    private final List<BasicBlock> blocks;

    public ExecutionPath(List<BasicBlock> blocks) {
        this.blocks = blocks;
    }

    private void findAll(BasicBlock parent, Predicate<BasicInstruction> predicate,
                         List<BasicInstruction> list, boolean recursive, List<BasicBlock> visited) {
        if (visited.contains(parent)) {
            return;
        }
        parent.instructions().forEach(insn -> {
            if (predicate.test(insn)) {
                list.add(insn);
            }
        });
        visited.add(parent);
        if (recursive) {
            parent.successors().forEach(block -> findAll(block, predicate, list, true, visited));
        }
    }

    /**
     * Finds all instructions matching the given predicate.
     *
     * @param predicate The predicate to match against.
     * @return A list of all instructions matching the given predicate.
     */
    public List<BasicInstruction> findAll(Predicate<BasicInstruction> predicate) {
        List<BasicBlock> visited = new ArrayList<>();
        List<BasicInstruction> result = new ArrayList<>();
        for (BasicBlock block : blocks) {
            findAll(block, predicate, result, true, visited);
        }
        return result;
    }

    private BasicInstruction findNext(BasicInstruction start, Predicate<BasicInstruction> predicate, int maxDist) {
        BasicInstruction insn = start;
        int jump = 0;
        while ((insn = insn.next()) != null && (jump == -1 || jump++ < maxDist)) {
            if (predicate.test(insn)) {
                return insn;
            }
        }
        return null;
    }

    private BasicInstruction findPrevious(BasicInstruction start, Predicate<BasicInstruction> predicate, int maxDist) {
        BasicInstruction insn = start;
        int jump = 0;
        while ((insn = insn.previous()) != null && (jump == -1 || jump++ < maxDist)) {
            if (predicate.test(insn)) {
                return insn;
            }
        }
        return null;
    }

    /**
     * Finds a list of results matching the given query.
     *
     * @param query The query to match.
     * @return A list of results matching the given query.
     */
    public List<FlowQueryResult> query(FlowQuery query) {
        List<FlowQueryResult> results = new ArrayList<>();
        List<Predicate<BasicInstruction>> predicates = query.predicates();
        if (predicates.isEmpty()) {
            return results;
        }
        List<BasicInstruction> lastMatch = null;
        boolean branching = false;
        FlowQuery.BranchType branchType = null;
        List<BasicInstruction> endings = new ArrayList<>();
        for (int i = 0; i < predicates.size(); i++) {
            Predicate<BasicInstruction> predicate = predicates.get(i);
            List<BasicInstruction> matching;
            if (branching) {
                List<BasicInstruction> branchInstructions = new ArrayList<>();
                for (BasicInstruction insn : lastMatch) {
                    Consumer<BasicBlock> consumer = (block -> {
                        branchInstructions.addAll(block.instructions);
                        block.instructions.forEach(bInsn -> bInsn.previous = insn);
                    });
                    if (branchType == FlowQuery.BranchType.TRUE) {
                        insn.block.trueBranch().ifPresent(consumer);
                    } else if (branchType == FlowQuery.BranchType.FALSE) {
                        insn.block.falseBranch().ifPresent(consumer);
                    } else {
                        insn.block.successors().forEach(consumer);
                    }
                }
                matching = branchInstructions.stream().filter(predicate::test).collect(Collectors.toList());
            } else {
                if (i == 0) {
                    matching = findAll(predicate);
                } else {
                    matching = new ArrayList<>();
                    for (BasicInstruction insn : lastMatch) {
                        BasicInstruction result = findNext(insn, predicate, query.distAt(i));
                        if (result != null) {
                            boolean loops = query.loopsAt(i);
                            boolean doesNotLoop = query.doesNotLoopAt(i);
                            if (loops || doesNotLoop) {
                                BasicInstruction parent = insn.parent();
                                BasicInstruction parentLabel = null;
                                while (parent != null && (parentLabel = findPrevious(parent, LABEL_PRED, 5)) == null) {
                                    parent = parent.parent();
                                }
                                if (parentLabel == null) {
                                    continue;
                                }
                                boolean valid = false;
                                List<BasicInstruction> jumps = new ArrayList<>();
                                List<BasicBlock> visited = new ArrayList<>();
                                findAll(insn.block, JUMP_PRED, jumps, false, visited);
                                if (!jumps.isEmpty()) {
                                    for (BasicInstruction jump : jumps) {
                                        LabelNode label = ((JumpInsnNode) jump.insn).label;
                                        if (label == parentLabel.insn) {
                                            valid = true;
                                        }
                                    }
                                }
                                if (!valid) {
                                    if (doesNotLoop) {
                                        result.previous = insn;
                                        matching.add(result);
                                    }
                                    continue;
                                }
                            }
                            result.previous = insn;
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
        for (BasicInstruction insn : endings) {
            List<BasicInstruction> hierarchy = new ArrayList<>();
            hierarchy.add(insn);
            while (insn.previous != null) {
                hierarchy.add(insn.previous);
                insn = insn.previous;
            }
            Collections.reverse(hierarchy);
            results.add(new FlowQueryResult(query, hierarchy));
        }
        return results;
    }

    /**
     * Prints out the path's BasicBlocks.
     */
    public void print() {
        List<BasicBlock> printed = new ArrayList<>();
        blocks.forEach(block -> block.print(printed));
    }
}
