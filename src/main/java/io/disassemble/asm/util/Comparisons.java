package io.disassemble.asm.util;

import java.util.Comparator;

/**
 * @author Tyler Sedlar
 * @since 5/28/17
 */
public class Comparisons {

    /**
     * Chains the given comparators together.
     *
     * @param comparators The comparisons to use.
     * @param <T> The type of comparators.
     * @return A single Comparator chained together with the given comparisons.
     */
    @SafeVarargs
    public static <T> Comparator<T> chain(Comparator<T>... comparators) {
        Comparator<T> root = comparators[0];
        for (int i = 1; i < comparators.length; i++) {
            root = root.thenComparing(comparators[i]);
        }
        return root;
    }
}
