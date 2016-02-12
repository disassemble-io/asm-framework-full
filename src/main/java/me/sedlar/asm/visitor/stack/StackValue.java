package me.sedlar.asm.visitor.stack;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 3/9/15
 */
public class StackValue implements Iterable<AbstractInsnNode> {

    public final BasicValue val;
    public List<AbstractInsnNode> insns = new LinkedList<>();

    public StackValue(BasicValue val) {
        this.val = val;
    }

    public StackValue(BasicValue val, AbstractInsnNode insn) {
        this(val);
        insns.add(insn);
    }

    public StackValue(BasicValue val, List<AbstractInsnNode> insns) {
        this(val);
        if (insns != null && !insns.isEmpty()) {
            this.insns = insns;
        }
    }

    public int size() {
        return insns.size();
    }

    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return insns.iterator();
    }

    @Override
    public String toString() {
        return val.toString();
    }
}
