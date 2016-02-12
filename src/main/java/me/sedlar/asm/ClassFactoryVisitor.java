package me.sedlar.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Tyler Sedlar
 * @since 3/9/15
 */
public class ClassFactoryVisitor extends ClassVisitor {

    protected ClassFactory factory;
    private int fieldIndex = 0, methodIndex = 0;

    public ClassFactoryVisitor() {
        super(Opcodes.ASM5);
    }

    public final void visit(int version, int access, String name, String signature, String superName,
                            String[] interfaces) {
        fieldIndex = 0;
        methodIndex = 0;
    }

    public void visitField(ClassField cf) {

    }

    public final FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        visitField(factory.fields[fieldIndex++]);
        return null;
    }

    public void visitMethod(ClassMethod cm) {

    }

    public final MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                           String[] exceptions) {
        visitMethod(factory.methods[methodIndex++]);
        return null;
    }
}