package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

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
        String mdesc = method.desc();
        return Arrays.binarySearch(PRIMITIVE_DESCRIPTORS, mdesc.charAt(mdesc.length() - 1)) >= 0;
    }
}
