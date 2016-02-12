package me.sedlar.asm.pattern.nano;

import me.sedlar.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
public abstract class AdvancedNanoPattern extends NanoPattern {

    @Override
    public final boolean matches(ClassMethod method) {
        return false;
    }
}
