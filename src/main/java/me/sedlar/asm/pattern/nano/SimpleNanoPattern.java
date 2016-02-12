package me.sedlar.asm.pattern.nano;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
public abstract class SimpleNanoPattern extends NanoPattern {

    @Override
    public final boolean matches(AbstractInsnNode insn) {
        return false;
    }
}
