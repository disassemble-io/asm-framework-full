package io.disassemble.asm.pattern.nano;

import io.disassemble.asm.ClassMethod;

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
