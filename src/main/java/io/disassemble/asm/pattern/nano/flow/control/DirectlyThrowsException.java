package io.disassemble.asm.pattern.nano.flow.control;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;

import static org.objectweb.asm.Opcodes.ATHROW;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Control Flow", name = "DirectlyThrowsException", simple = true,
        description = "contains the athrow instruction")
public class DirectlyThrowsException extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.count(ATHROW) > 0;
    }
}
