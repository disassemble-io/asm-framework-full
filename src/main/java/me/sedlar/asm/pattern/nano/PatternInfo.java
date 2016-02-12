package me.sedlar.asm.pattern.nano;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PatternInfo {

    String category();

    String name();

    boolean simple();

    String description();
}
