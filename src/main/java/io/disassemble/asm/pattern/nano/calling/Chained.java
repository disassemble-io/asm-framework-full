package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import io.disassemble.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
@PatternInfo(category = "Calling", name = "Chained", simple = true, description = "returns itself")
public class Chained extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.chained();
    }
}
