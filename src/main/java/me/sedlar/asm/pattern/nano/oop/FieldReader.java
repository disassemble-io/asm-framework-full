package me.sedlar.asm.pattern.nano.oop;

import me.sedlar.asm.pattern.nano.AdvancedNanoPattern;
import me.sedlar.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Object-Orientation", name = "FieldReader", simple = false,
    description = "reads (static or instance) field values from an object")
public class FieldReader extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() == GETFIELD || insn.getOpcode() == GETSTATIC;
    }
}
