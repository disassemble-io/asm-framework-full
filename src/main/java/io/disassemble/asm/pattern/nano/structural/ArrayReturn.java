package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import org.objectweb.asm.Type;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "ArrayReturn", simple = true, description = "returns an array")
public class ArrayReturn extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return Type.getReturnType(method.desc()).getDescriptor().startsWith("[");
    }
}
