package me.sedlar.asm.pattern.nano.flow.data;

import me.sedlar.asm.pattern.nano.AdvancedNanoPattern;
import me.sedlar.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Data Flow", name = "LocalReader", simple = false,
    description = "reads values of local variables on stack frame")
public class LocalReader extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() >= ILOAD && insn.getOpcode() <= ALOAD;
    }
}
