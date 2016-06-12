package io.disassemble.asm.visitor;

import io.disassemble.asm.ClassMethodVisitor;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ComplexityVisitor extends ClassMethodVisitor {

    private static final int[] EDGE_CASES = {
            IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT,
            IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, IFNULL, IFNONNULL
    };

    private int edges = 0, nodes = 0;
    private int complexity = 1;

    /**
     * Gets the amount of edges visited.
     * 
     * @return The amount of edges visited.
     */
    public int edges() {
        return edges;
    }

    /**
     * Gets the amount of nodes visited.
     * 
     * @return The amount of nodes visited.
     */
    public int nodes() {
        return nodes;
    }

    /**
     * Gets the mccabe cyclomatic complexity.
     * 
     * @return The mccabe cyclomatic complexity.
     */
    public int mccabe() {
        return edges - nodes + 2;
    }

    /**
     * Gets the standard complexity of the method visited.
     * 
     * @return The standard complexity of the method visited.
     */
    public int complexity() {
        return complexity;
    }

    private void incrementBoth() {
        edges++;
        nodes++;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        edges = 0;
        nodes = 0;
        complexity = 1;
    }

    @Override
    public void visitFieldInsn(FieldInsnNode fin) {
        incrementBoth();
    }

    @Override
    public void visitIincInsn(IincInsnNode iin) {
        incrementBoth();
    }

    @Override
    public void visitInsn(InsnNode in) {
        incrementBoth();
    }

    @Override
    public void visitIntInsn(IntInsnNode iin) {
        incrementBoth();
    }

    @Override
    public void visitJumpInsn(JumpInsnNode jin) {
        incrementBoth();
        edges++;
        for (int edge : EDGE_CASES) {
            if (jin.getOpcode() == edge) {
                complexity++;
                return;
            }
        }
    }

    @Override
    public void visitLabel(LabelNode ln) {
        nodes++;
    }

    @Override
    public void visitLdcInsn(LdcInsnNode lin) {
        incrementBoth();
    }

    @Override
    public void visitLookupSwitchInsn(LookupSwitchInsnNode lsin) {
        nodes++;
        edges += lsin.labels.size();
        complexity += lsin.labels.size();
    }

    @Override
    public void visitMethodInsn(MethodInsnNode min) {
        incrementBoth();
    }

    @Override
    public void visitMultiANewArrayInsn(MultiANewArrayInsnNode manain) {
        incrementBoth();
    }

    @Override
    public void visitTableSwitchInsn(TableSwitchInsnNode tsin) {
        nodes++;
        edges += tsin.labels.size();
        complexity += tsin.labels.size();
    }

    @Override
    public void visitTypeInsn(TypeInsnNode tin) {
        incrementBoth();
    }

    @Override
    public void visitVarInsn(VarInsnNode vin) {
        incrementBoth();
    }
}