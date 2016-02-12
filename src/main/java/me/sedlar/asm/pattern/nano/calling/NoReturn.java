package me.sedlar.asm.pattern.nano.calling;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "NoReturn", simple = true, description = "returns void")
public class NoReturn extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().endsWith(")V");
    }
}
