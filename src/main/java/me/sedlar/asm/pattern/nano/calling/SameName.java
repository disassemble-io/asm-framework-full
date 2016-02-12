package me.sedlar.asm.pattern.nano.calling;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "SameName", simple = true,
        description = "calls another method with the same name")
public class SameName extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.calls(min -> !min.owner.equals(method.owner.name()) && min.name.equals(method.name()));
    }
}
