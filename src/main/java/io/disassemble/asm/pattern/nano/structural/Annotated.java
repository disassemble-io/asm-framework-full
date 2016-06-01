package io.disassemble.asm.pattern.nano.structural;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

/**
 * @author Christopher Carpenter
 */
@PatternInfo(category = "Structural", name = "Annotated", simple = true, description = "has a runtime visible annotation")
public class Annotated extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        List<AnnotationNode> runtimeAnnotations = method.method.visibleAnnotations;
        return runtimeAnnotations != null && !runtimeAnnotations.isEmpty();
    }
}