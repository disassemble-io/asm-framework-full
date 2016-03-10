package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import io.disassemble.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "SameName", simple = true,
    description = "calls another method with the same name")
public class SameName extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.calls(min -> !min.owner.equals(method.owner.name()) && min.name.equals(method.name()));
    }
}
