package io.disassemble.asm;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 3/8/15
 */
public class ClassFactory {

    public final ClassNode node;
    public ClassField[] fields;
    public ClassMethod[] methods;

    public ClassFactory(ClassNode node) {
        this.node = node;
        this.fields = new ClassField[node.fields.size()];
        for (int i = 0; i < this.fields.length; i++) {
            this.fields[i] = new ClassField(this, node.fields.get(i));
        }
        this.methods = new ClassMethod[node.methods.size()];
        for (int i = 0; i < this.methods.length; i++) {
            this.methods[i] = new ClassMethod(this, node.methods.get(i));
        }
    }

    /**
     * Gets this class' name.
     *
     * @return The name of this class.
     */
    public String name() {
        return node.name;
    }

    /**
     * Gets this class' desc.
     *
     * @return The desc of this class.
     */
    public String desc() {
        return "L" + name() + ";";
    }

    /**
     * Sets this class' name.
     *
     * @param name The name to set this class to.
     */
    public void setName(String name) {
        node.name = name;
    }

    /**
     * Gets this class' superName.
     *
     * @return The superName of this class.
     */
    public String superName() {
        return node.superName;
    }

    /**
     * Sets this class' superName.
     *
     * @param superName The name to set this class' superName to.
     */
    public void setSuperName(String superName) {
        node.superName = superName;
    }

    /**
     * Checks whether this class has an owner or not.
     *
     * @return <t>true</t> if this class does not have an owner, otherwise <t>false</t>.
     */
    public boolean ownerless() {
        return superName().equals("java/lang/Object");
    }

    /**
     * Gets this class' implemented interfaces.
     *
     * @return The implemented interfaces of this class.
     */
    @SuppressWarnings("unchecked")
    public List<String> interfaces() {
        return (List<String>) node.interfaces;
    }

    /**
     * Gets this class' access.
     *
     * @return The access of this class.
     */
    public int access() {
        return node.access;
    }

    /**
     * Sets this class' access.
     *
     * @param access The access flags to set this class' access to.
     */
    public void setAccess(int access) {
        node.access = access;
    }

    /**
     * Removes the given field from this class.
     *
     * @param field The field to remove.
     */
    public void remove(ClassField field) {
        if (!node.fields.contains(field.field)) {
            return;
        }
        node.fields.remove(field.field);
        ClassField[] fields = new ClassField[this.fields.length - 1];
        int idx = 0;
        for (ClassField cf : this.fields) {
            if (cf.equals(field)) {
                continue;
            }
            fields[idx++] = cf;
        }
        this.fields = fields;
    }

    /**
     * Removes the given method from this class.
     *
     * @param method The method to remove.
     */
    public void remove(ClassMethod method) {
        if (!node.methods.contains(method.method)) {
            return;
        }
        node.methods.remove(method.method);
        ClassMethod[] methods = new ClassMethod[this.methods.length - 1];
        int idx = 0;
        for (ClassMethod cm : this.methods) {
            if (cm.equals(method)) {
                continue;
            }
            methods[idx++] = cm;
        }
        this.methods = methods;
    }

    /**
     * Calls ClassNode#accept with the given visitor.
     *
     * @param cfv The visitor to call.
     */
    public void accept(ClassFactoryVisitor cfv) {
        cfv.factory = this;
        node.accept(cfv);
    }

    /**
     * Calls ClassMethod#accept with every method in this class.
     *
     * @param cmv The visitor to call.
     */
    public void dispatch(ClassMethodVisitor cmv) {
        for (ClassMethod method : methods) {
            method.accept(cmv);
        }
    }

    /**
     * Finds the first field matching the given predicate.
     *
     * @param predicate The predicate to match.
     * @return The first field matching the given predicate.
     */
    public ClassField findField(Predicate<ClassField> predicate) {
        for (ClassField field : fields) {
            if (predicate.test(field)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Finds the fields matching the given predicate.
     *
     * @param predicate The predicate to match.
     * @return The fields matching the given predicate.
     */
    public List<ClassField> findFields(Predicate<ClassField> predicate) {
        List<ClassField> valid = new LinkedList<>();
        for (ClassField field : fields) {
            if (predicate.test(field)) {
                valid.add(field);
            }
        }
        return valid;
    }

    /**
     * Finds the amount of fields matching the given desc.
     *
     * @param desc          The desc to match.
     * @param includeStatic An option to include static fields or not.
     * @return The amount of fields matching the given desc.
     */
    public int fieldCount(String desc, boolean includeStatic) {
        return findFields(f -> {
            if (f.local() || (!f.local() && includeStatic)) {
                if (desc == null || f.desc().equals(desc)) {
                    return true;
                }
            }
            return false;
        }).size();
    }

    /**
     * Finds the amount of fields matching the given desc.
     *
     * @param desc The desc to match.
     * @return The amount of fields matching the given desc.
     */
    public int fieldCount(String desc) {
        return fieldCount(desc, false);
    }

    /**
     * The amount of fields in this class.
     *
     * @return The amount of fields in this class.
     */
    public int fieldCount() {
        return fieldCount(null, false);
    }

    /**
     * Finds the amount of different field descs in this class.
     *
     * @param includeStatic An option to include static fields or not.
     * @return The amount of different field descs in this class.
     */
    public int fieldTypeCount(boolean includeStatic) {
        Set<String> descs = new HashSet<>();
        return findFields(f -> {
            if (f.local() || (!f.local() && includeStatic)) {
                if (!descs.contains(f.desc())) {
                    descs.add(f.desc());
                    return true;
                }
            }
            return false;
        }).size();
    }

    /**
     * Finds the amount of different field descs in this class.
     *
     * @return The amount of different field descs in this class.
     */
    public int fieldTypeCount() {
        return fieldTypeCount(false);
    }

    /**
     * Finds the amount of non-jdk fields within this class.
     *
     * @param includeStatic An option to include static fields or not.
     * @return The amount of non-jdk fields within this class.
     */
    public int abnormalFieldCount(boolean includeStatic) {
        return findFields(f -> {
            if (f.local() || (!f.local() && includeStatic)) {
                if (f.desc().contains("L") && f.desc().endsWith(";") && !f.desc().contains("java")) {
                    return true;
                }
            }
            return false;
        }).size();
    }

    /**
     * Finds the amount of non-jdk fields within this class.
     *
     * @return The amount of non-jdk fields within this class.
     */
    public int abnormalFieldCount() {
        return abnormalFieldCount(false);
    }

    /**
     * Finds the first method matching the given predicate.
     *
     * @param predicate The predicate to match.
     * @return The first method matching the given predicate.
     */
    public ClassMethod findMethod(Predicate<ClassMethod> predicate) {
        for (ClassMethod method : methods) {
            if (predicate.test(method)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Finds the methods  matching the given predicate.
     *
     * @param predicate The predicate to match.
     * @return The methods matching the given predicate.
     */
    public List<ClassMethod> findMethods(Predicate<ClassMethod> predicate) {
        List<ClassMethod> valid = new LinkedList<>();
        for (ClassMethod method : methods) {
            if (predicate.test(method)) {
                valid.add(method);
            }
        }
        return valid;
    }

    /**
     * Finds the amount of methods matching the given desc.
     *
     * @param desc          The desc to match.
     * @param includeStatic An option to include static methods or not.
     * @return The amount of methods matching the given desc.
     */
    public int methodCount(String desc, boolean includeStatic) {
        return findMethods(m -> {
            if (m.local() || (!m.local() && includeStatic)) {
                if (desc == null || m.desc().equals(desc)) {
                    return true;
                }
            }
            return false;
        }).size();
    }

    /**
     * Finds the amount of methods matching the given desc.
     *
     * @param desc The desc to match.
     * @return The amount of methods matching the given desc.
     */
    public int methodCount(String desc) {
        return methodCount(desc, false);
    }

    /**
     * The amount of methods in this class.
     *
     * @return The amount of methods in this class.
     */
    public int methodCount() {
        return methodCount(null, false);
    }

    /**
     * Finds the amount of different method descs in this class.
     *
     * @param includeStatic An option to include static method or not.
     * @return The amount of different method descs in this class.
     */
    public int methodTypeCount(boolean includeStatic) {
        Set<String> descs = new HashSet<>();
        return findMethods(m -> {
            if (m.local() || (!m.local() && includeStatic)) {
                if (!descs.contains(m.desc())) {
                    descs.add(m.desc());
                    return true;
                }
            }
            return false;
        }).size();
    }

    /**
     * Finds the amount of different method descs in this class.
     *
     * @return The amount of different method descs in this class.
     */
    public int methodTypeCount() {
        return fieldTypeCount(false);
    }

    /**
     * Gets a list of the constructor descs in this class.
     *
     * @return A list of the constructor descs in this class.
     */
    public List<String> constructors() {
        List<String> descs = new LinkedList<>();
        findMethods(m -> {
            if (m.name().equals("<init>")) {
                descs.add(m.desc());
                return true;
            }
            return false;
        });
        return descs;
    }
}