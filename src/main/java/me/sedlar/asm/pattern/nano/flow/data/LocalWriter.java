package me.sedlar.asm.pattern.nano.flow.data;

import me.sedlar.asm.pattern.nano.AdvancedNanoPattern;
import me.sedlar.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Data Flow", name = "LocalWriter", simple = false,
        description = "writes values of local variables on stack frame")
public class LocalWriter extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() >= ISTORE && insn.getOpcode() <= ASTORE;
    }
}
