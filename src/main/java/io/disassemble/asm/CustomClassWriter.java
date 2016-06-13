package io.disassemble.asm;

import org.objectweb.asm.ClassWriter;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 *
 * Allows for custom classes not included in the classloader to have their frames expanded.
 */
public class CustomClassWriter extends ClassWriter {

    private final Archive archive;

    public CustomClassWriter(Archive archive, int flags) {
        super(flags);
        this.archive = archive;
    }

    @Override
    public String getCommonSuperClass(String a, String b) {
        try {
            return super.getCommonSuperClass(a, b);
        } catch (Exception e) {
            if (archive.classes().containsKey(a) && archive.classes().containsKey(b)) {
                String aSuper = archive.classes().get(a).superName();
                String bSuper = archive.classes().get(b).superName();
                if (aSuper.equals(bSuper)) {
                    return aSuper;
                }
            }
            return "java/lang/Object";
        }
    }
}
