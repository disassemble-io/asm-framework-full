package me.sedlar.asm.visitor.flow;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class FlowQuery {

    public static final int DEFAULT_MAX_DISTANCE = 10;

    private final List<Predicate<ExecutionNode>> predicates = new ArrayList<>();
    private final List<Integer> branches = new ArrayList<>();
    private final Map<Integer, Integer> dists = new HashMap<>();
    private final Map<Integer, String> names = new HashMap<>();

    public List<Predicate<ExecutionNode>> predicates() {
        return predicates;
    }

    /**
     * Chains the given predicate as a query.
     *
     * @param predicate The predicate to chain.
     * @return This FlowQuery chained with the given predicate.
     */
    public FlowQuery query(Predicate<ExecutionNode> predicate) {
        predicates.add(predicate);
        return this;
    }

    /**
     * Chains the given opcodes as an opcode predicate.
     *
     * @param opcodes The possible opcodes to query for.
     * @return This FlowQuery chained with the given opcodes.
     */
    public FlowQuery opcode(int... opcodes) {
        Arrays.sort(opcodes);
        predicates.add(insn -> (Arrays.binarySearch(opcodes, insn.source.instruction.getOpcode()) >= 0));
        return this;
    }

    /**
     * Branches at the prior chained predicate.
     *
     * @return This FlowQuery with a branch at the prior predicate.
     */
    public FlowQuery branch() {
        branches.add(predicates.size() - 1);
        return this;
    }

    /**
     * Checks whether there is a branch at the given index.
     *
     * @param index The index to check for a branch at.
     * @return <t>true</t> if there is a branch at the given index, otherwise <t>false</t>.
     */
    public boolean branchesAt(int index) {
        return branches.contains(index);
    }

    /**
     * Sets the search distance for the prior predicate.
     *
     * @param maxDist The maximum distance to search.
     * @return This FlowQuery with a distance constraint at the prior predicate.
     */
    public FlowQuery dist(int maxDist) {
        dists.put(predicates.size() - 1, maxDist);
        return this;
    }

    /**
     * Gets the max search distance for the predicate at the given index.
     *
     * @param index The index to check at.
     * @return The max search distance for the predicate at the given index.
     */
    public int distAt(int index) {
        return (dists.containsKey(index) ? dists.get(index) : DEFAULT_MAX_DISTANCE);
    }

    /**
     * Gives the prior predicate the given name.
     *
     * @param name The name to give the prior predicate.
     * @return This FlowQuery with a name at the prior predicate.
     */
    public FlowQuery name(String name) {
        names.put(predicates.size() - 1, name);
        return this;
    }

    /**
     * Gets the name for the predicate at the given index.
     *
     * @param index The index to check at.
     * @return The name for the predicate at the given index.
     */
    public String nameAt(int index) {
        return names.get(index);
    }
}
