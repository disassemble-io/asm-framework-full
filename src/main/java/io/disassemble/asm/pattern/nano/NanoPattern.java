package io.disassemble.asm.pattern.nano;

import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;
/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
public abstract class NanoPattern {

    public PatternInfo info() {
        return getClass().getAnnotation(PatternInfo.class);
    }

    public abstract boolean matches(AbstractInsnNode insn);

    public abstract boolean matches(ClassMethod method);
}
