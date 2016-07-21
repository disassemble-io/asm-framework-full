package io.disassemble.asm.util;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

/**
 * @author Tyler Sedlar
 * @author Christopher Carpenter
 * @since 2/1/16
 */
public class Assembly {
    private static final String[] JAVA_IDENTIFIERS = {"abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while"};

    static {
        Arrays.sort(JAVA_IDENTIFIERS);
    }

    private Assembly() {
    }

    /**
     * Creates a predicate that matches any of the given opcodes.
     *
     * @param opcodes The opcodes to match.
     * @return A predicate that matches any of the given opcodes.
     */
    public static Predicate<AbstractInsnNode> createOpcodePredicate(int... opcodes) {
        Arrays.sort(opcodes);
        return insn -> (Arrays.binarySearch(opcodes, insn.getOpcode()) >= 0);
    }

    /**
     * Gets a list of instructions matching the given predicate.
     *
     * @param list      The list of instructions.
     * @param predicate The predicate to match.
     * @param <E>       A subclass of {@code AbstractInsnNode}
     * @return A list of instructions matching the given predicate.
     */
    @SuppressWarnings("unchecked")
    public static <E extends AbstractInsnNode> List<E> findAll(InsnList list, Predicate<AbstractInsnNode> predicate) {
        List<E> results = new ArrayList<>();
        AbstractInsnNode[] instructions = list.toArray();
        for (AbstractInsnNode insn : instructions) {
            if (predicate.test(insn)) {
                results.add((E) insn);
            }
        }
        return results;
    }

    /**
     * Checks how many instructions from the given list match the given predicate.
     *
     * @param list      The list of instructions.
     * @param predicate The predicate to match.
     * @return The amount of instructions from the given list matching the given predicate.
     */
    public static int count(InsnList list, Predicate<AbstractInsnNode> predicate) {
        return findAll(list, predicate).size();
    }

    /**
     * Finds the first occurrence of an instruction matching the given predicate.
     *
     * @param list      The list of instructions.
     * @param predicate The predicate to match.
     * @param <E>       A subclass of {@code AbstractInsnNode}
     * @return The first occurrence of an instruction matching the given predicate.
     */
    @SuppressWarnings("unchecked")
    public static <E extends AbstractInsnNode> E findFirst(InsnList list, Predicate<AbstractInsnNode> predicate) {
        AbstractInsnNode[] instructions = list.toArray();
        for (AbstractInsnNode insn : instructions) {
            if (predicate.test(insn)) {
                return (E) insn;
            }
        }
        return null;
    }

    /**
     * Finds The first occurrence of an instruction matching the given predicate, occurring after the given instruction.
     *
     * @param insn      The instruction to search after.
     * @param predicate The predicate to match.
     * @param dist      The maximum distance to search.
     * @param <E>       A subclass of {@code AbstractInsnNode}
     * @return The first occurrence of an instruction matching the given predicate, occurring after the given instruction.
     */
    @SuppressWarnings("unchecked")
    public static <E extends AbstractInsnNode> E findNext(AbstractInsnNode insn, Predicate<AbstractInsnNode> predicate,
                                                          int dist) {
        int jump = 0;
        while ((insn = insn.getNext()) != null && (dist == -1 || jump++ < dist)) {
            if (predicate.test(insn)) {
                return (E) insn;
            }
        }
        return null;
    }

    /**
     * Finds The first occurrence of an instruction matching the given predicate, occurring after the given instruction.
     *
     * @param insn      The instruction to search after.
     * @param predicate The predicate to match.
     * @param <E>       A subclass of {@code AbstractInsnNode}
     * @return The first occurrence of an instruction matching the given predicate, occurring after the given instruction.
     */
    public static <E extends AbstractInsnNode> E findNext(AbstractInsnNode insn, Predicate<AbstractInsnNode> predicate) {
        return findNext(insn, predicate, -1);
    }

    /**
     * Finds The first occurrence of an instruction matching the given predicate, occurring before the given instruction.
     *
     * @param insn      The instruction to search after.
     * @param predicate The predicate to match.
     * @param dist      The maximum distance to search.
     * @param <E>       A subclass of {@code AbstractInsnNode}
     * @return The first occurrence of an instruction matching the given predicate, occurring before the given instruction.
     */
    @SuppressWarnings("unchecked")
    public static <E extends AbstractInsnNode> E findPrevious(AbstractInsnNode insn,
                                                              Predicate<AbstractInsnNode> predicate, int dist) {
        int jump = 0;
        while ((insn = insn.getPrevious()) != null && (dist == -1 || jump++ < dist)) {
            if (predicate.test(insn)) {
                return (E) insn;
            }
        }
        return null;
    }

    /**
     * Finds The first occurrence of an instruction matching the given predicate, occurring before the given instruction.
     *
     * @param insn      The instruction to search after.
     * @param predicate The predicate to match.
     * @param <E>       A subclass of {@code AbstractInsnNode}
     * @return The first occurrence of an instruction matching the given predicate, occurring before the given instruction.
     */
    public static <E extends AbstractInsnNode> E findPrevious(AbstractInsnNode insn,
                                                              Predicate<AbstractInsnNode> predicate) {
        return findPrevious(insn, predicate, -1);
    }

    /**
     * Gets the name of the given opcode.
     *
     * @param opcode The opcode.
     * @return The name of the given opcode.
     */
    public static String opname(int opcode) {
        if (opcode >= 0 && opcode < Printer.OPCODES.length) {
            return Printer.OPCODES[opcode];
        }
        return Integer.toString(opcode);
    }

    /**
     * Renames the given field throughout the ClassFactory map with to given name.
     *
     * @param classes The map of classes to rename within.
     * @param fn      The field to rename.
     * @param newName The name to rename the field to.
     */
    public static void rename(Map<String, ClassFactory> classes, ClassField fn, String newName) {
        for (ClassFactory factory : classes.values()) {
            for (ClassMethod method : factory.methods) {
                for (AbstractInsnNode ain : method.instructions().toArray()) {
                    if (ain instanceof FieldInsnNode) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        ClassFactory realOwner = classes.get(fin.owner);
                        while (realOwner != null) {
                            if (realOwner.findField(cf -> cf.name().equals(fin.name)) != null) {
                                break;
                            }
                            realOwner = classes.get(realOwner.superName());
                        }
                        if (realOwner != null && realOwner.name().equals(fn.owner.name()) &&
                                fin.name.equals(fn.field.name)) {
                            fin.name = newName;
                        }
                    }
                }
            }
        }
        fn.field.name = newName;
    }

    /**
     * Renames the given method throughout the ClassFactory map with to given name.
     *
     * @param classes The map of classes to rename within.
     * @param cm      The method to rename.
     * @param newName The name to rename the method to.
     */
    public static void rename(Map<String, ClassFactory> classes, ClassMethod cm, String newName) {
        for (ClassFactory factory : classes.values()) {
            for (ClassMethod method : factory.methods) {
                for (AbstractInsnNode ain : method.instructions().toArray()) {
                    if (ain instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) ain;
                        ClassFactory realOwner = classes.get(min.owner);
                        while (realOwner != null) {
                            if (realOwner.findMethod(
                                    m -> m.name().equals(min.name) && m.desc().equals(min.desc)
                            ) != null) {
                                break;
                            }
                            realOwner = classes.get(realOwner.superName());
                        }
                        if (realOwner != null && realOwner.name().equals(cm.owner.name()) &&
                                min.name.equals(cm.method.name)) {
                            min.name = newName;
                        }
                    }
                }
            }
        }
        cm.method.name = newName;
    }

    /**
     * Renames the given class throughout the ClassFactory map with to given name.
     *
     * @param classes The map of classes to rename within.
     * @param cf      The class to rename.
     * @param newName The name to rename the class to.
     */
    public static void rename(Map<String, ClassFactory> classes, ClassFactory cf, String newName) {
        for (ClassFactory factory : classes.values()) {
            if (factory.superName().equals(cf.name())) {
                factory.setSuperName(newName);
            }
            if (factory.interfaces().contains(cf.name())) {
                factory.interfaces().remove(cf.name());
                factory.interfaces().add(newName);
            }
            for (ClassField field : factory.fields) {
                if (field.desc().endsWith("L" + cf.name() + ";")) {
                    field.setDescriptor(field.desc().replace("L" + cf.name() + ";", "L" + newName + ";"));
                }
            }
            for (ClassMethod method : factory.methods) {
                if (method.desc().contains("L" + cf.name() + ";")) {
                    method.setDescriptor(method.desc().replaceAll("L" + cf.name() + ";", "L" + newName + ";"));
                }
                for (AbstractInsnNode ain : method.instructions().toArray()) {
                    if (ain instanceof FieldInsnNode) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (fin.owner.equals(cf.name())) {
                            fin.owner = newName;
                        }
                        if (fin.desc.contains("L" + cf.name() + ";")) {
                            fin.desc = fin.desc.replace("L" + cf.name() + ";", "L" + newName + ";");
                        }
                    } else if (ain instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) ain;
                        if (min.owner.equals(cf.name())) {
                            min.owner = newName;
                        }
                        if (min.desc.contains("L" + cf.name() + ";")) {
                            min.desc = min.desc.replaceAll("L" + cf.name() + ";", "L" + newName + ";");
                        }
                    } else if (ain instanceof TypeInsnNode) {
                        TypeInsnNode tin = (TypeInsnNode) ain;
                        if (tin.desc.equals(cf.name())) {
                            tin.desc = newName;
                        } else if (tin.desc.contains("L" + cf.name() + ";")) {
                            tin.desc = tin.desc.replace("L" + cf.name() + ";", "L" + newName + ";");
                        }
                    } else if (ain instanceof MultiANewArrayInsnNode) {
                        MultiANewArrayInsnNode manain = (MultiANewArrayInsnNode) ain;
                        if (manain.desc.contains("L" + cf.name() + ";")) {
                            manain.desc = manain.desc.replace("L" + cf.name() + ";", "L" + newName + ";");
                        }
                    } else if (ain instanceof LdcInsnNode) {
                        LdcInsnNode ldc = (LdcInsnNode) ain;
                        Object cst = ldc.cst;
                        if (cst != null && cst instanceof String) {
                            String cstString = (String) cst;
                            if (cstString.startsWith(cf.name() + ".") && cstString.contains("(")) {
                                ldc.cst = cstString.replace(cf.name() + ".", newName + ".");
                            }
                        }
                    }
                }
            }
        }
        cf.setName(newName);
    }

    /**
     * Checks whether the given instructions are similar to each other.
     *
     * @param insn1 The first instruction to compare.
     * @param insn2 The second instruction to compare.
     * @return true if the instructions are similar, otherwise false.
     */
    public static boolean instructionsEqual(AbstractInsnNode insn1, AbstractInsnNode insn2) {
        if (insn1 == insn2) {
            return true;
        }
        if (insn1 == null || insn2 == null) {
            return false;
        }
        if (insn1.getType() != insn2.getType() || insn1.getOpcode() != insn2.getOpcode()) {
            return false;
        }
        int size;
        switch (insn1.getType()) {
            case INSN: {
                return true;
            }
            case INT_INSN: {
                IntInsnNode iin1 = (IntInsnNode) insn1, iin2 = (IntInsnNode) insn2;
                return iin1.operand == iin2.operand;
            }
            case VAR_INSN: {
                VarInsnNode vin1 = (VarInsnNode) insn1, vin2 = (VarInsnNode) insn2;
                return vin1.var == vin2.var;
            }
            case TYPE_INSN: {
                TypeInsnNode tin1 = (TypeInsnNode) insn1, tin2 = (TypeInsnNode) insn2;
                return tin1.desc.equals(tin2.desc);
            }
            case FIELD_INSN: {
                FieldInsnNode fin1 = (FieldInsnNode) insn1, fin2 = (FieldInsnNode) insn2;
                return fin1.desc.equals(fin2.desc) && fin1.name.equals(fin2.name) && fin1.owner.equals(fin2.owner);
            }
            case METHOD_INSN: {
                MethodInsnNode min1 = (MethodInsnNode) insn1, min2 = (MethodInsnNode) insn2;
                return min1.desc.equals(min2.desc) && min1.name.equals(min2.name) && min1.owner.equals(min2.owner);
            }
            case INVOKE_DYNAMIC_INSN: {
                InvokeDynamicInsnNode idin1 = (InvokeDynamicInsnNode) insn1, idin2 = (InvokeDynamicInsnNode) insn2;
                return idin1.bsm.equals(idin2.bsm) && Arrays.equals(idin1.bsmArgs, idin2.bsmArgs) &&
                        idin1.desc.equals(idin2.desc) && idin1.name.equals(idin2.name);
            }
            case JUMP_INSN: {
                JumpInsnNode jin1 = (JumpInsnNode) insn1, jin2 = (JumpInsnNode) insn2;
                return instructionsEqual(jin1.label, jin2.label);
            }
            case LABEL: {
                Label label1 = ((LabelNode) insn1).getLabel(), label2 = ((LabelNode) insn2).getLabel();
                return label1 == null ? label2 == null : label1.info == null ? label2.info == null :
                        label1.info.equals(label2.info);
            }
            case LDC_INSN: {
                LdcInsnNode lin1 = (LdcInsnNode) insn1, lin2 = (LdcInsnNode) insn2;
                return lin1.cst.equals(lin2.cst);
            }
            case IINC_INSN: {
                IincInsnNode iiin1 = (IincInsnNode) insn1, iiin2 = (IincInsnNode) insn2;
                return iiin1.incr == iiin2.incr && iiin1.var == iiin2.var;
            }
            case TABLESWITCH_INSN: {
                TableSwitchInsnNode tsin1 = (TableSwitchInsnNode) insn1, tsin2 = (TableSwitchInsnNode) insn2;
                size = tsin1.labels.size();
                if (size != tsin2.labels.size()) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    if (!instructionsEqual((LabelNode) tsin1.labels.get(i), (LabelNode) tsin2.labels.get(i))) {
                        return false;
                    }
                }
                return instructionsEqual(tsin1.dflt, tsin2.dflt) && tsin1.max == tsin2.max && tsin1.min == tsin2.min;
            }
            case LOOKUPSWITCH_INSN: {
                LookupSwitchInsnNode lsin1 = (LookupSwitchInsnNode) insn1, lsin2 = (LookupSwitchInsnNode) insn2;
                size = lsin1.labels.size();
                if (size != lsin2.labels.size()) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    if (!instructionsEqual((LabelNode) lsin1.labels.get(i), (LabelNode) lsin2.labels.get(i))) {
                        return false;
                    }
                }
                return instructionsEqual(lsin1.dflt, lsin2.dflt) && lsin1.keys.equals(lsin2.keys);
            }
            case MULTIANEWARRAY_INSN: {
                MultiANewArrayInsnNode manain1 = (MultiANewArrayInsnNode) insn1, manain2 = (MultiANewArrayInsnNode) insn2;
                return manain1.desc.equals(manain2.desc) && manain1.dims == manain2.dims;
            }
            case FRAME: {
                FrameNode fn1 = (FrameNode) insn1, fn2 = (FrameNode) insn2;
                return fn1.local.equals(fn2.local) && fn1.stack.equals(fn2.stack);
            }
            case LINE: {
                LineNumberNode lnn1 = (LineNumberNode) insn1, lnn2 = (LineNumberNode) insn2;
                return lnn1.line == lnn2.line && instructionsEqual(lnn1.start, lnn2.start);
            }
        }
        return false;
    }

    /**
     * Checks whether the given instruction arrays are similar.
     *
     * @param insns  The first instruction array to compare.
     * @param insns2 The second instruction array to compare.
     * @return true if the given instruction arrays are similar, otherwise false.
     */
    public static boolean instructionsEqual(AbstractInsnNode[] insns, AbstractInsnNode[] insns2) {
        if (insns == insns2) {
            return true;
        }
        if (insns == null || insns2 == null) {
            return false;
        }
        int length = insns.length;
        if (insns2.length != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            AbstractInsnNode insn1 = insns[i], insn2 = insns2[i];
            if (!(insn1 == null ? insn2 == null : instructionsEqual(insn1, insn2))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gives a string representation of the given instruction.
     *
     * @param insn The instruction to represent.
     * @return A string representation of the given instruction.
     */
    public static String toString(AbstractInsnNode insn) {
        if (insn == null) {
            return "null";
        }
        int op = insn.getOpcode();
        if (op == -1) {
            if (insn instanceof LabelNode) {
                return (insn.getClass().getSimpleName() + insn.toString().split(insn.getClass().getSimpleName())[1]);
            }
            return insn.toString();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(Printer.OPCODES[op]);
        switch (insn.getType()) {
            case INT_INSN: {
                builder.append(' ');
                builder.append(((IntInsnNode) insn).operand);
                break;
            }
            case VAR_INSN: {
                builder.append(' ');
                builder.append('#');
                builder.append(((VarInsnNode) insn).var);
                break;
            }
            case TYPE_INSN: {
                builder.append(' ');
                builder.append(((TypeInsnNode) insn).desc);
                break;
            }
            case FIELD_INSN: {
                FieldInsnNode fin = (FieldInsnNode) insn;
                builder.append(' ');
                builder.append(fin.owner);
                builder.append('.');
                builder.append(fin.name);
                builder.append(' ');
                builder.append(fin.desc);
                break;
            }
            case METHOD_INSN: {
                MethodInsnNode min = (MethodInsnNode) insn;
                builder.append(' ');
                builder.append(min.owner);
                builder.append('.');
                builder.append(min.name);
                builder.append(' ');
                builder.append(min.desc);
                break;
            }
            case JUMP_INSN:
            case TABLESWITCH_INSN:
            case LOOKUPSWITCH_INSN: {
                if (op == Opcodes.GOTO) {
                    JumpInsnNode jin = (JumpInsnNode) insn;
                    builder.append(' ');
                    builder.append(Assembly.toString(jin.label));
                }
                break;
            }
            case LDC_INSN: {
                Object cst = ((LdcInsnNode) insn).cst;
                builder.append(' ');
                builder.append(cst.getClass().getName());
                builder.append(' ');
                builder.append(cst);
                break;
            }
            case IINC_INSN: {
                IincInsnNode iin = (IincInsnNode) insn;
                builder.append(' ');
                builder.append('#');
                builder.append(iin.var);
                builder.append(' ');
                builder.append(iin.incr);
                break;
            }
            case MULTIANEWARRAY_INSN: {
                MultiANewArrayInsnNode m = (MultiANewArrayInsnNode) insn;
                builder.append(' ');
                builder.append(m.desc);
                builder.append(' ');
                builder.append(m.dims);
                break;
            }
        }
        return builder.toString();
    }

    public static boolean isJavaIdentifier(String string) {
        return Arrays.binarySearch(JAVA_IDENTIFIERS, string) >= 0;
    }
}
