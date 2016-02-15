package me.sedlar.asm.updater;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AnalyzerInfo {

    String[] hooks();
}
