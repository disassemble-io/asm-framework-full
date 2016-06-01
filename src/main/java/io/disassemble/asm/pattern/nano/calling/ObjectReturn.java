package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "ObjectReturn", simple = true, description = "returns an object")
public class ObjectReturn extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().endsWith(";");
    }
}
