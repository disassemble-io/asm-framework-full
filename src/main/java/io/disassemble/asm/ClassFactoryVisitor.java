package io.disassemble.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Tyler Sedlar
 * @since 3/9/15
 */
public abstract class ClassFactoryVisitor extends ClassVisitor {
    protected ClassFactory factory;
    private ClassFactoryVisitor cfv;
    private int fieldIndex = 0, methodIndex = 0;

    public ClassFactoryVisitor() {
        super(Opcodes.ASM5);
    }

    public ClassFactoryVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    public ClassFactoryVisitor(ClassFactoryVisitor cfv) {
        super(Opcodes.ASM5, cfv);
        this.cfv = cfv;
    }

    public final void visit(int version, int access, String name, String signature, String superName,
                            String[] interfaces) {
        fieldIndex = 0;
        methodIndex = 0;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public void visitField(ClassField cf) {
        if (cfv != null) {
            cfv.visitField(cf);
        }
    }

    public final FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        visitField(factory.fields[fieldIndex++]);
        return super.visitField(access, name, desc, signature, value);
    }

    public void visitMethod(ClassMethod cm) {
        if (cfv != null) {
            cfv.visitMethod(cm);
        }
    }

    public final MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                           String[] exceptions) {
        visitMethod(factory.methods[methodIndex++]);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}