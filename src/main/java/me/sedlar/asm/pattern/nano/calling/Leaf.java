package me.sedlar.asm.pattern.nano.calling;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;
import org.objectweb.asm.util.Printer;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "Leaf", simple = true, description = "does not issue any method calls")
public class Leaf extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.count(insn -> insn.getOpcode() != -1 &&
            Printer.OPCODES[insn.getOpcode()].startsWith("INVOKE")) == 0;
    }
}
