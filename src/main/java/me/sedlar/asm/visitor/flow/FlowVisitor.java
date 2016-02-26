package me.sedlar.asm.visitor.flow;

import jdk.nashorn.internal.ir.Block;
import me.sedlar.asm.ClassMethodVisitor;
import me.sedlar.asm.util.Assembly;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/25/2016
 */
public class FlowVisitor extends ClassMethodVisitor {

    private ControlFlowGraph graph;
    private List<TryCatchBlockNode>[] handlers;
    private boolean visitExceptions = true;


    private List<AbstractInsnNode> visited = new ArrayList<>();
    private List<AbstractInsnNode> currentInstructions = new ArrayList<>();
    private List<BasicBlock> blocks = new ArrayList<>();
    private int blockStart, blockEnd;

    @SuppressWarnings("unchecked")
    public void setGraphData(ControlFlowGraph graph, int instructionSize, boolean visitExceptions) {
        reset();
        this.graph = graph;
        this.handlers = (List<TryCatchBlockNode>[]) new ArrayList<?>[instructionSize];
        this.visitExceptions = visitExceptions;
        this.visited.clear();
        this.currentInstructions.clear();
        this.blocks.clear();
    }

    private BasicBlock findBlockWithIndex(int index) {
        for (BasicBlock block : blocks) {
            if (index >= block.start && index <= block.end) {
                return block;
            }
        }
        return null;
    }

    private void addBlock(int to) {
        BasicBlock block = new BasicBlock(graph, blockStart, to);
        block.instructions.addAll(currentInstructions);
        currentInstructions.clear();
        blocks.add(block);
    }

    private void newControlFlowEdge(int from, int to) {
        AbstractInsnNode fromInsn = method.instructions().get(from);
        AbstractInsnNode toInsn = method.instructions().get(to);
        boolean backwards = (to < from);
        graph.add(fromInsn, toInsn, backwards);
        if (!visited.contains(fromInsn)) {
            if (currentInstructions.isEmpty()) {
                blockStart = from;
            }
            currentInstructions.add(fromInsn);
            if (fromInsn instanceof JumpInsnNode) {
                addBlock(from);
            }
            visited.add(fromInsn);
        }
        blockEnd = to;
    }

    private void newControlFlowExceptionEdge(int from, int to) {
        AbstractInsnNode fromInsn = method.instructions().get(from);
        AbstractInsnNode toInsn = method.instructions().get(to);
        boolean backwards = (to < from);
        graph.exception(fromInsn, toInsn, backwards);
    }

    private void visitInstructionExceptionEdge(int index) {
        if (visitExceptions) {
            List<TryCatchBlockNode> insnHandlers = handlers[index];
            if (insnHandlers != null) {
                for (TryCatchBlockNode tcb : insnHandlers) {
                    newControlFlowExceptionEdge(index, method.instructions().indexOf(tcb.handler));
                }
            }
        }
    }

    private void visitInstructionEdge(AbstractInsnNode insn) {
        if (graph == null) {
            throw new IllegalStateException("FlowVisitor#setGraphData has not been called");
        }
        int opcode = insn.getOpcode();
        if (opcode != ATHROW && (opcode < IRETURN || opcode > RETURN)) {
            int index = method.instructions().indexOf(insn);
            newControlFlowEdge(index, index + 1);
            visitInstructionExceptionEdge(index);
        } else {
            currentInstructions.add(insn);
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
        for (Object tcb : method.method.tryCatchBlocks) {
            TryCatchBlockNode node = (TryCatchBlockNode) tcb;
            int begin = method.instructions().indexOf(node.start);
            int end = method.instructions().indexOf(node.end);
            for (int j = begin; j < end; ++j) {
                List<TryCatchBlockNode> insnHandlers = handlers[j];
                if (insnHandlers == null) {
                    insnHandlers = new ArrayList<>();
                    handlers[j] = insnHandlers;
                }
                insnHandlers.add(node);
            }
        }
    }

    @Override
    public void visitEnd() {
        addBlock(blockEnd);
        for (BasicBlock block : blocks) {
            block.endNode().ifPresent(endNode -> {
                List<ControlFlowNode> succNodes = endNode.successors;
                for (ControlFlowNode succNode : succNodes) {
                    BasicBlock succBlock = findBlockWithIndex(method.instructions().indexOf(succNode.instruction));
                    if (succBlock != null) {
                        block.successors.add(succBlock);
                        succBlock.predecessor = block;
                    }
                }
            });
        }
        graph.blocks = new ArrayList<>(blocks);
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
