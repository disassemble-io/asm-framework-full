package io.disassemble.asm.visitor.flow;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 2/13/2016
 */
public class FlowQueryResult {

    private final FlowQuery query;
    private final List<BasicInstruction> instructions;

    public FlowQueryResult(FlowQuery query, List<BasicInstruction> instructions) {
        this.query = query;
        this.instructions = instructions;
    }

    /**
     * Finds the BasicInstruction with the given name.
     *
     * @param name The name to search for.
     * @return The BasicInstruction with the given name.
     */
    public Optional<BasicInstruction> findBasicInstruction(String name) {
        for (int i = 0; i < instructions.size(); i++) {
            String nodeName = query.nameAt(i);
            if (nodeName != null && nodeName.equals(name)) {
                return Optional.ofNullable(instructions.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the AbstractInsnNode with the given name.
     *
     * @param name The name to search for.
     * @return The AbstractInsnNode with the given name.
     */
    public Optional<AbstractInsnNode> findInstruction(String name) {
        Optional<BasicInstruction> insn = findBasicInstruction(name);
        return (insn.isPresent() ? Optional.ofNullable(insn.get().insn) : Optional.empty());
    }

    /**
     * Gets a map of the named instructions from the FlowQuery.
     *
     * @return A map of the named instructions from the FlowQuery.
     */
    public Map<String, AbstractInsnNode> namedInstructions() {
        Map<String, AbstractInsnNode> instructions = new HashMap<>();
        for (int i = 0; i < this.instructions.size(); i++) {
            String nodeName = query.nameAt(i);
            if (nodeName != null) {
                instructions.put(nodeName, this.instructions.get(i).insn);
            }
        }
        return instructions;
    }
}
