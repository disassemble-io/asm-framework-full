package io.disassemble.asm.pattern.nano.flow.data;

import io.disassemble.asm.pattern.nano.AdvancedNanoPattern;
import io.disassemble.asm.pattern.nano.PatternInfo;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Data Flow", name = "ArrayReader", simple = false, description = "reads values from an array")
public class ArrayReader extends AdvancedNanoPattern {

    @Override
    public boolean matches(AbstractInsnNode insn) {
        return insn.getOpcode() >= IALOAD && insn.getOpcode() <= SALOAD;
    }
}
