package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "SpecifiesException", simple = true, description = "specifies that it can throw an exception")
public class SpecifiesException extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return !method.exceptions().isEmpty();
    }
}