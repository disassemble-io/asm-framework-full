package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import org.objectweb.asm.Type;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "ClassReturn", simple = true, description = "returns a class type object")
public class ClassReturn extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        //Detects non-array objects
        return Type.getReturnType(method.desc()).getDescriptor().startsWith("L");
    }
}
