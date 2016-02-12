package me.sedlar.asm.pattern.nano.flow.control;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Control Flow", name = "Looping", simple = true,
        description = "one or more control flow loops in method body")
public class Looping extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        List<String> visitedLabels = new ArrayList<>();
        AbstractInsnNode[] instructions = method.instructions().toArray();
        for (AbstractInsnNode insn : instructions) {
            if (insn instanceof JumpInsnNode) {
                Label label = ((JumpInsnNode) insn).label.getLabel();
                String labelString = label.toString();
                if (visitedLabels.contains(labelString)) {
                    return true;
                } else {
                    visitedLabels.add(labelString);
                }
            }
        }
        return false;
    }
}
