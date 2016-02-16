package me.sedlar.asm.pattern.nano.flow.control;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.PatternInfo;
import me.sedlar.asm.pattern.nano.SimpleNanoPattern;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Control Flow", name = "StraightLine", simple = true,
    description = "no branches in method body")
public class StraightLine extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.count(insn -> insn instanceof JumpInsnNode || insn instanceof TableSwitchInsnNode ||
            insn instanceof LookupSwitchInsnNode) == 0;
    }
}
