package me.sedlar.asm.pattern.nano.composite;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.NanoPatternTypes;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
public abstract class CompositePattern implements Opcodes, NanoPatternTypes {

    public abstract String[] simples();

    public abstract String[] advanced();

    public boolean matches(ClassMethod method) {
        List<String> methodSimples = method.findSimpleNanoPatterns();
        String[] simples = simples();
        for (String simpleLabel : simples) {
            if (!methodSimples.contains(simpleLabel)) {
                return false;
            }
        }
        List<String> methodAdvanced = method.findAdvancedNanoPatterns();
        String[] advanced = advanced();
        for (String advancedLabel : advanced) {
            if (!methodAdvanced.contains(advancedLabel)) {
                return false;
            }
        }
        return true;
    }
}
