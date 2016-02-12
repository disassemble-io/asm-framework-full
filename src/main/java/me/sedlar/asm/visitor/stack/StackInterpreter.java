package me.sedlar.asm.visitor.stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 3/9/15
 */
public class StackInterpreter extends BasicInterpreter implements Opcodes {

    public final Map<BasicValue, List<AbstractInsnNode>> values = new HashMap<>();

    private void addEntry(BasicValue val, AbstractInsnNode insn) {
        if (!values.containsKey(val)) {
            values.put(val, new LinkedList<>());
        }
        values.get(val).add(insn);
    }

    @Override
    public BasicValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        BasicValue superVal = super.newOperation(insn);
        if (superVal != null) {
            addEntry(superVal, insn);
        }
        return superVal;
    }

    @Override
    public BasicValue unaryOperation(AbstractInsnNode insn, BasicValue val) throws AnalyzerException {
        BasicValue superVal = super.unaryOperation(insn, val);
        if (superVal != null) {
            addEntry(superVal, insn);
        }
        return superVal;
    }

    @Override
    public BasicValue binaryOperation(AbstractInsnNode insn, BasicValue val1, BasicValue val2) throws AnalyzerException {
        BasicValue superVal = super.binaryOperation(insn, val1, val2);
        if (superVal != null) {
            addEntry(superVal, insn);
        }
        return superVal;
    }

    @Override
    public BasicValue naryOperation(AbstractInsnNode insn, List vals) throws AnalyzerException {
        BasicValue superVal = super.naryOperation(insn, vals);
        if (superVal != null) {
            addEntry(superVal, insn);
        }
        return superVal;
    }
}