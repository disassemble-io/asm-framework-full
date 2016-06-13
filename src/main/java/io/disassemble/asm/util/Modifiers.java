package io.disassemble.asm.util;

import org.objectweb.asm.Opcodes;

public class Modifiers {
    public static int remove(int modifier, int flag) {
        return modifier & ~flag;
    }

    public static int add(int modifier, int flag) {
        return modifier | flag;
    }

    public static boolean marked(int modifier, int flag) {
        return (modifier & flag) == flag;
    }

    public static int markPublic(int current_modifier) {
        current_modifier = add(current_modifier, Opcodes.ACC_PUBLIC);
        current_modifier = remove(current_modifier, Opcodes.ACC_PROTECTED);
        current_modifier = remove(current_modifier, Opcodes.ACC_PRIVATE);
        return current_modifier;
    }

    public static int markProtected(int current_modifier) {
        current_modifier = remove(current_modifier, Opcodes.ACC_PUBLIC);
        current_modifier = add(current_modifier, Opcodes.ACC_PROTECTED);
        current_modifier = remove(current_modifier, Opcodes.ACC_PRIVATE);
        return current_modifier;
    }

    public static int markPrivate(int current_modifier) {
        current_modifier = remove(current_modifier, Opcodes.ACC_PUBLIC);
        current_modifier = remove(current_modifier, Opcodes.ACC_PROTECTED);
        current_modifier = add(current_modifier, Opcodes.ACC_PRIVATE);
        return current_modifier;
    }

    public static int markPackageLocal(int current_modifier) {
        current_modifier = remove(current_modifier, Opcodes.ACC_PUBLIC);
        current_modifier = remove(current_modifier, Opcodes.ACC_PROTECTED);
        current_modifier = remove(current_modifier, Opcodes.ACC_PRIVATE);
        return current_modifier;
    }
}
