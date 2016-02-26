package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/26/2016
 */
public class BasicBlock {

    public final ControlFlowGraph cfg;
    public final int start, end;

    protected final List<BasicInstruction> instructions = new ArrayList<>();

    protected BasicBlock predecessor;
    protected List<BasicBlock> successors = new ArrayList<>();

    public BasicBlock(ControlFlowGraph cfg, int start, int end, List<AbstractInsnNode> insns) {
        this.cfg = cfg;
        this.start = start;
        this.end = end;
        instructions.addAll(insns.stream().map(insn -> new BasicInstruction(this, insn)).collect(Collectors.toList()));
    }

    /**
     * Gets the starting node of this block.
     *
     * @return The starting node of this block.
     */
    public Optional<ControlFlowNode> startNode() {
        if (instructions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cfg.nodeFor(instructions.get(0).insn, false));
    }

    /**
     * Gets the starting instruction of this block.
     *
     * @return The starting instruction of this block.
     */
    public Optional<BasicInstruction> startInstruction() {
        if (instructions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(instructions.get(0));
    }

    /**
     * Gets the ending node of this block.
     *
     * @return The ending node of this block.
     */
    public Optional<ControlFlowNode> endNode() {
        if (instructions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cfg.nodeFor(instructions.get(instructions.size() - 1).insn, false));
    }

    /**
     * Gets the ending instruction of this block.
     *
     * @return The ending instruction of this block.
     */
    public Optional<BasicInstruction> endInstruction() {
        if (instructions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(instructions.get(instructions.size() -1));
    }

    /**
     * Gets a list of instructions within this block.
     *
     * @return A list of instructions within this block.
     */
    public List<BasicInstruction> instructions() {
        return instructions;
    }

    /**
     * Gets this block's predecessor.
     *
     * @return This block's predecessor.
     */
    public BasicBlock predecessor() {
        return predecessor;
    }

    /**
     * Gets the list of successors for this BasicBlock.
     *
     * @return The list of sucessors for this BasicBlock.
     */
    public List<BasicBlock> successors() {
        return successors;
    }

    /**
     * Gets the path that branches to a true value.
     *
     * @return The path that branches to a true value.
     */
    public Optional<BasicBlock> trueBranch() {
        if (successors.size() == 1) {
            return Optional.of(successors.get(0));
        }
        BasicInstruction endInsn = endInstruction().orElse(null);
        if (endInsn.insn instanceof JumpInsnNode) {
            LabelNode label = ((JumpInsnNode) endInsn.insn).label;
            for (BasicBlock successor : successors) {
                BasicInstruction startInsn = successor.startInstruction().orElse(null);
                if (startInsn != null && label == startInsn.insn) {
                    return Optional.of(successor);
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
    public Optional<BasicBlock> falseBranch() {
        BasicInstruction endInsn = endInstruction().orElse(null);
        if (endInsn.insn instanceof JumpInsnNode) {
            LabelNode label = ((JumpInsnNode) endInsn.insn).label;
            for (BasicBlock successor : successors) {
                BasicInstruction startInsn = successor.startInstruction().orElse(null);
                if (startInsn == null || label != startInsn.insn) {
                    return Optional.of(successor);
                }
            }
        }
        return Optional.empty();
    }

    private void print(String prepend, String suffix) {
        String result = (prepend + "<" + start + " - " + end + ">\n");
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) {
                result += "\n";
            }
            result += (prepend + Assembly.toString(instructions.get(i).insn));
        }
        result += suffix;
        System.out.println(result);
    }

    private void printBlock(BasicBlock block, String prefix, List<BasicBlock> printed) {
        if (!printed.contains(block)) {
            printed.add(block);
            boolean hasSuccessor = false;
            for (BasicBlock successor : block.successors) {
                if (!printed.contains(successor)) {
                    hasSuccessor = true;
                }
            }
            block.print(prefix, (hasSuccessor ? " { " : ""));
            if (hasSuccessor) {
                block.successors.forEach(succ -> {
                    System.out.println(prefix + "  {");
                    printBlock(succ, prefix + "    ", printed);
                    System.out.println(prefix + "  }");
                    printed.add(succ);
                });
                System.out.println(prefix + "}");
            }
        }
    }

    /**
     * Prints the block out in a readable manner.
     *
     * @param printed An empty or pre-filled list used for preventing StackOverflowExceptions
     */
    public void print(List<BasicBlock> printed) {
        printBlock(this, "", printed);
    }

    /**
     * Prints the block out in a readable manner.
     */
    public void print() {
        List<BasicBlock> printed = new ArrayList<>();
        print(printed);
    }
}
