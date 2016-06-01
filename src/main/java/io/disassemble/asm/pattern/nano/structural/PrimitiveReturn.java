package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import jdk.internal.org.objectweb.asm.Type;

import java.util.Arrays;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "PrimitiveReturn", simple = true, description = "returns a primitive")
public class PrimitiveReturn extends SimpleNanoPattern {
    private static final char[] PRIMITIVE_DESCRIPTORS = {'Z', 'C', 'B', 'S', 'I', 'J', 'F', 'D'};

    static {
        Arrays.sort(PRIMITIVE_DESCRIPTORS);
    }

    @Override
    public boolean matches(ClassMethod method) {
        return Arrays.binarySearch(PRIMITIVE_DESCRIPTORS, Type.getReturnType(method.desc()).getDescriptor().charAt(0)) >= 0;
    }
}
