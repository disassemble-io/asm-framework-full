package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Structural", name = "NoParameters", simple = true, description = "takes no paramaters")
public class NoParameters extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().startsWith("()");
    }
}
