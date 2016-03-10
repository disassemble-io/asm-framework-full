package io.disassemble.asm.pattern.nano.composite;

import io.disassemble.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
public class BasicChainedSetterPattern extends CompositePattern {

    @Override
    public String[] simples() {
        return new String[]{CHAINED, STRAIGHT_LINE};
    }

    @Override
    public String[] advanced() {
        return new String[]{LOCAL_READER};
    }

    @Override
    public boolean matches(ClassMethod method) {
        return super.matches(method) && BasicSetterPattern.setterMatches(method);
    }
}
