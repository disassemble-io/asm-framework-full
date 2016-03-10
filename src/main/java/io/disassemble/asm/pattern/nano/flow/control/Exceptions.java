package io.disassemble.asm.pattern.nano.flow.control;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Control Flow", name = "Exceptions", simple = true,
    description = "may throw an unhandled exception")
public class Exceptions extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.count(ATHROW) > 0;
    }
}
