package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 2/26/2016
 */
public class BasicBlock {

    public final ControlFlowGraph cfg;
    public final int start, end;

    protected final List<AbstractInsnNode> instructions = new ArrayList<>();

    protected BasicBlock predecessor;
    protected List<BasicBlock> successors = new ArrayList<>();

    public BasicBlock(ControlFlowGraph cfg, int start, int end) {
        this.cfg = cfg;
        this.start = start;
        this.end = end;
    }

    public Optional<ControlFlowNode> startNode() {
        if (instructions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cfg.nodeFor(instructions.get(0), false));
    }

    public Optional<ControlFlowNode> endNode() {
        if (instructions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cfg.nodeFor(instructions.get(instructions.size() - 1), false));
    }

    private void print(String prepend, String suffix) {
        String result = (prepend + "<" + start + " - " + end + ">\n");
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) {
                result += "\n";
            }
            result += (prepend + Assembly.toString(instructions.get(i)));
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

    public void print(List<BasicBlock> printed) {
        printBlock(this, "", printed);
    }

    public void print() {
        List<BasicBlock> printed = new ArrayList<>();
        print(printed);
    }
}
