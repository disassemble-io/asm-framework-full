package io.disassemble.asm.program;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ParserInfo {

    String name();

    String[] hooks();
}
