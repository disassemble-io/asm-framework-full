package me.sedlar.asm.pattern.nano.oop;

import me.sedlar.asm.pattern.nano.AdvancedNanoPattern;
import me.sedlar.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Object-Orientation", name = "FieldWriter", simple = false,
    description = "writes values to (static or instance) field of an object")
public class FieldWriter extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() == PUTFIELD || insn.getOpcode() == PUTSTATIC;
    }
}
