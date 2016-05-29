package io.disassemble.asm.pattern.nano.oop;

import io.disassemble.asm.pattern.nano.AdvancedNanoPattern;
import io.disassemble.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Object-Orientation", name = "TypeManipulator", simple = false,
        description = "uses type casts or instanceof operations")
public class TypeManipulator extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() == CHECKCAST || insn.getOpcode() == INSTANCEOF;
    }
}
