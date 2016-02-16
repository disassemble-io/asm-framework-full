package me.sedlar.asm.pattern.nano.calling;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "Recursive", simple = true, description = "calls itself recursively")
public class Recursive extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.calls(min -> min.owner.equals(method.owner.name()) && min.name.equals(method.name()) &&
            min.desc.equals(method.desc()));
    }
}
