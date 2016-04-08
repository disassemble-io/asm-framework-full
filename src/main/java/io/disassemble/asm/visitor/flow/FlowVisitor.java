package io.disassemble.asm.visitor.flow;

import io.disassemble.asm.ClassMethodVisitor;
import io.disassemble.asm.util.AlphaLabel;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 4/7/2016
 */
public class FlowVisitor extends ClassMethodVisitor {

    private final Map<Integer, List<Integer>> successors = new HashMap<>();
    private List<Integer> visited = new ArrayList<>();
    protected Map<Integer, BasicBlock> blocks = new HashMap<>();
    private List<Integer> currentInstructions = new ArrayList<>();

    private int blockStart, blockEnd;

    private ControlFlowGraph graph;

    public void setGraph(ControlFlowGraph graph) {
        this.graph = graph;
    }

    private void newControlFlowEdge(int from, int to) {
        if (!successors.containsKey(from)) {
            successors.put(from, new ArrayList<>());
        }
        successors.get(from).add(to);
        if (!visited.contains(from)) {
            if (currentInstructions.isEmpty()) {
                blockStart = from;
            }
            currentInstructions.add(from);
            AbstractInsnNode fromInsn = method.instructions().get(from);
            if (fromInsn instanceof JumpInsnNode) {
                addBlock(from);
            }
            visited.add(from);
        }
        blockEnd = to;
    }

    private void addBlock(int to) {
        blocks.put(blockStart, new BasicBlock(AlphaLabel.get(blocks.size()), method, blockStart, to,
                currentInstructions));
        currentInstructions.clear();
    }

    private void visitInstructionEdge(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        int index = method.instructions().indexOf(insn);
        if (opcode != ATHROW && (opcode < IRETURN || opcode > RETURN)) {
            newControlFlowEdge(index, index + 1);
        } else {
            currentInstructions.add(index);
        }
    }

    @Override
    public void visitJumpInsn(JumpInsnNode jin) {
        int opcode = jin.getOpcode();
        int index = method.instructions().indexOf(jin);
        if (opcode != GOTO && opcode != JSR) {
            newControlFlowEdge(index, index + 1);
        }
        int jump = method.instructions().indexOf(jin.label);
        newControlFlowEdge(index, jump);
    }

    @Override
    public void visitTableSwitchInsn(TableSwitchInsnNode tsin) {
        int index = method.instructions().indexOf(tsin);
        int dfltJump = method.instructions().indexOf(tsin.dflt);
        newControlFlowEdge(index, dfltJump);
        for (Object label : tsin.labels) {
            int jump = method.instructions().indexOf((LabelNode) label);
            newControlFlowEdge(index, jump);
        }
    }

    @Override
    public void visitLookupSwitchInsn(LookupSwitchInsnNode lsin) {
        int index = method.instructions().indexOf(lsin);
        int dfltJump = method.instructions().indexOf(lsin.dflt);
        newControlFlowEdge(index, dfltJump);
        for (Object label : lsin.labels) {
            int jump = method.instructions().indexOf((LabelNode) label);
            newControlFlowEdge(index, jump);
        }
    }

    @Override
    public void visitCode() {

    }

    @Override
    public void visitEnd() {
        addBlock(blockEnd);
        for (BasicBlock block : blocks.values()) {
            List<Integer> succs = successors.get(block.end);
            if (succs != null) {
                for (int successor : succs) {
                    BasicBlock succBlock = blocks.get(successor);
                    if (succBlock != null) {
                        block.successors.add(succBlock);
                        succBlock.predecessor = block;
                    }
                }
            }
        }
        graph.blocks.addAll(blocks.values());
        super.reset();
        this.successors.clear();
        this.visited.clear();
        this.blocks.clear();
        this.currentInstructions.clear();
        this.blockStart = -1;
    }

    @Override
    public void visitLabel(LabelNode ln) {
        visitInstructionEdge(ln);
    }

    @Override
    public void visitFrame(FrameNode fn) {
        visitInstructionEdge(fn);
    }

    @Override
    public void visitInsn(InsnNode in) {
        visitInstructionEdge(in);
    }

    @Override
    public void visitIntInsn(IntInsnNode iin) {
        visitInstructionEdge(iin);
    }

    @Override
    public void visitVarInsn(VarInsnNode vin) {
        visitInstructionEdge(vin);
    }

    @Override
    public void visitTypeInsn(TypeInsnNode tin) {
        visitInstructionEdge(tin);
    }

    @Override
    public void visitFieldInsn(FieldInsnNode fin) {
        visitInstructionEdge(fin);
    }

    @Override
    public void visitMethodInsn(MethodInsnNode min) {
        visitInstructionEdge(min);
    }

    @Override
    public void visitInvokeDynamicInsn(InvokeDynamicInsnNode idin) {
        visitInstructionEdge(idin);
    }

    @Override
    public void visitLdcInsn(LdcInsnNode lin) {
        visitInstructionEdge(lin);
    }

    @Override
    public void visitIincInsn(IincInsnNode iin) {
        visitInstructionEdge(iin);
    }

    @Override
    public void visitMultiANewArrayInsn(MultiANewArrayInsnNode manai) {
        visitInstructionEdge(manai);
    }

    @Override
    public void visitLineNumber(LineNumberNode lnn) {
        visitInstructionEdge(lnn);
    }
}