package me.sedlar.asm.pattern.nano;

import me.sedlar.asm.ClassMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
public abstract class NanoPattern implements Opcodes {

    public PatternInfo info() {
        return getClass().getAnnotation(PatternInfo.class);
    }

    public abstract boolean matches(AbstractInsnNode insn);

    public abstract boolean matches(ClassMethod method);
}
