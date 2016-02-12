package me.sedlar.asm.pattern.nano.composite;

import me.sedlar.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
public class BasicSetterPattern extends CompositePattern {

    @Override
    public String[] simples() {
        return new String[]{NO_RETURN, STRAIGHT_LINE};
    }

    @Override
    public String[] advanced() {
        return new String[]{LOCAL_READER};
    }

    @Override
    public boolean matches(ClassMethod method) {
        return super.matches(method) && setterMatches(method);
    }

    protected static boolean setterMatches(ClassMethod method) {
        List<String> simples = method.findSimpleNanoPatterns();
        if (simples.contains(LEAF)) {
            return method.findAdvancedNanoPatterns().contains(FIELD_WRITER);
        } else {
            AbstractInsnNode[] instructions = method.instructions().toArray();
            for (AbstractInsnNode insn : instructions) {
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    String key = (min.owner + "." + min.name + min.desc);
                    ClassMethod resolved = ClassMethod.resolve(key);
                    if (resolved != null && resolved.findAdvancedNanoPatterns().contains(FIELD_WRITER)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
