package me.sedlar.asm.pattern.nano.calling;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
@PatternInfo(category = "Calling", name = "Chained", simple = true, description = "returns itself")
public class Chained extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.chained();
    }
}
