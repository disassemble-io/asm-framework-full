package io.disassemble.asm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/24/16
 */
public class Grep {

    private final String pattern;

    /**
     * Creates a Grep object based on the given pattern.
     *
     * @param pattern The basic grep pattern to be used.
     */
    public Grep(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Executes this Grep's pattern on the given string.
     *
     * @param test The string to be tested for matches.
     * @return The matches in a map denoted by their {label}
     */
    public Map<String, String> exec(String test) {
        Map<String, String> matches = new HashMap<>();
        int start = -1, prevEnd = 0, lookup = 0;
        String tag = "";
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{') {
                start = i;
            } else if (start != -1 && !tag.isEmpty() && c == '}') {
                String backwards = pattern.substring(prevEnd, start);
                int idx = test.indexOf(backwards, lookup);
                if (idx == -1) {
                    return null;
                }
                // endIdx might actually need to be improved for inner string vars if it contains
                // whatever character comes after the grouping.
                int endIdx = test.indexOf(pattern.charAt(i + 1), lookup + 1);
                if (endIdx == -1) {
                    return null;
                }
                matches.put(tag, test.substring(idx + backwards.length(), endIdx));
                lookup = idx;
                prevEnd = (i + 1);
                tag = "";
                start = -1;
            } else if (start != -1) {
                tag += c;
            }
        }
        return matches;
    }
}
