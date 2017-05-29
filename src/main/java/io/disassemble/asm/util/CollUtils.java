package io.disassemble.asm.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 5/28/17
 */
public class CollUtils {

    /**
     * Improved Collections#min to use an Optional instead throwing an exception.
     *
     * @param list The list to check.
     * @param <T> The type of list.
     * @return The minimum value within the list.
     */
    public static <T extends Object & Comparable<? super T>> Optional<T> min(Collection<T> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Collections.min(list));
        }
    }

    /**
     * Improved Collections#max to use an Optional instead throwing an exception.
     *
     * @param list The list to check.
     * @param <T> The type of list.
     * @return The maximum value within the list.
     */
    public static <T extends Object & Comparable<? super T>> Optional<T> max(Collection<T> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Collections.max(list));
        }
    }
}
