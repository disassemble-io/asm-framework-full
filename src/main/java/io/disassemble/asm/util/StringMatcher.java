package io.disassemble.asm.util;

/**
 * @author Tyler Sedlar
 * @since 3/10/15
 */
public class StringMatcher {

    public static boolean matches(String checker, String threshold) {
        if (checker.length() >= 2) {
            char selector = checker.charAt(0);
            char implier = checker.charAt(1);
            if (implier == '>') {
                String trimmed = checker.substring(2);
                switch (selector) {
                    case '*': {
                        return threshold.contains(trimmed);
                    }
                    case '$': {
                        return threshold.endsWith(trimmed);
                    }
                    case '!': {
                        return !threshold.equals(trimmed);
                    }
                    case '^': {
                        return threshold.startsWith(trimmed);
                    }
                    case '~': {
                        return threshold.matches(trimmed);
                    }
                    case '-': {
                        return !threshold.contains(trimmed);
                    }
                    default: {
                        return threshold.equals(checker);
                    }
                }
            }
        }
        return threshold.equals(checker);
    }
}
