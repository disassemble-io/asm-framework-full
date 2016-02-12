package me.sedlar.asm.pattern.nano.oop;

import me.sedlar.asm.pattern.nano.AdvancedNanoPattern;
import me.sedlar.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Object-Orientation", name = "ObjectCreator", simple = false, description = "creates objects")
public class ObjectCreator extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() == Opcodes.NEW;
    }
}
