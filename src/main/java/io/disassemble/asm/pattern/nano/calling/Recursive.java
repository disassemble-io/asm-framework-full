package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import io.disassemble.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "Recursive", simple = true, description = "calls itself recursively")
public class Recursive extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.calls(min -> min.owner.equals(method.owner.name()) && min.name.equals(method.name()) &&
            min.desc.equals(method.desc()));
    }
}
