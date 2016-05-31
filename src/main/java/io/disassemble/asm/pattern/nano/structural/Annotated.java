package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "Annotated", simple = true, description = "has a runtime visible annotation")
public class Annotated extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.method.visibleAnnotations!=null&&!method.method.visibleAnnotations.isEmpty();
    }
}