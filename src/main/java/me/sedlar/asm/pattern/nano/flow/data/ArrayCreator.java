package me.sedlar.asm.pattern.nano.flow.data;

import me.sedlar.asm.pattern.nano.AdvancedNanoPattern;
import me.sedlar.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Data Flow", name = "ArrayCreator", simple = false, description = "creates a new array")
public class ArrayCreator extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() == NEWARRAY || insn.getOpcode() == ANEWARRAY || insn.getOpcode() == MULTIANEWARRAY;
    }
}
