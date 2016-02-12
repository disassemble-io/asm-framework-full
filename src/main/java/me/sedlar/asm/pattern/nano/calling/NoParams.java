package me.sedlar.asm.pattern.nano.calling;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "NoParams", simple = true, description = "takes no arguments")
public class NoParams extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().startsWith("()");
    }
}
