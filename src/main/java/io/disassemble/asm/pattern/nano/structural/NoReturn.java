package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Structural", name = "NoReturn", simple = true, description = "returns void")
public class NoReturn extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().endsWith(")V");
    }
}
