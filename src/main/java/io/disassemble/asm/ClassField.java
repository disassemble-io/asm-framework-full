package io.disassemble.asm;

import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 3/8/15
 */
public class ClassField {

    public final ClassFactory owner;
    public final FieldNode field;

    public ClassField(ClassFactory owner, FieldNode field) {
        this.owner = owner;
        this.field = field;
    }

    public String name() {
        return field.name;
    }

    public void setName(String name) {
        field.name = name;
    }

    public String desc() {
        return field.desc;
    }

    public void setDescriptor(String desc) {
        field.desc = desc;
    }

    public int access() {
        return field.access;
    }

    public void setAccess(int access) {
        field.access = access;
    }

    public String key() {
        return owner.name() + "." + name();
    }

    public boolean local() {
        return (access() & ACC_STATIC) == 0;
    }

    public void remove() {
        owner.remove(this);
    }

    @Override
    public String toString() {
        return key();
    }
}