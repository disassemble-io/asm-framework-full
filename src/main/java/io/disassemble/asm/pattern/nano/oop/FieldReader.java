package io.disassemble.asm.pattern.nano.oop;

import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.AdvancedNanoPattern;
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
