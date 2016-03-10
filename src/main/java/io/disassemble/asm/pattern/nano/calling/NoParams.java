package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import io.disassemble.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "NoParams", simple = true, description = "takes no arguments")
public class NoParams extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().startsWith("()");
    }
}
