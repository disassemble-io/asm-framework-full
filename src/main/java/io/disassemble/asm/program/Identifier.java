package io.disassemble.asm.program;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.program.log.DefaultIdentifierLogger;
import io.disassemble.asm.program.log.IdentifierLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public abstract class Identifier extends Thread {

    private final Map<String, ClassFactory> classes;
    private final List<BytecodeParser> parsers = new ArrayList<>();

    private IdentifierLogger logger = new DefaultIdentifierLogger();

    public Identifier(Map<String, ClassFactory> classes) {
        this.classes = classes;
    }

    public abstract void populateParsers(List<BytecodeParser> parsers);

    /**
     * Obtains the IdentifierInfo annotation for this Identifier.
     *
     * @return The IdentifierInfo annotation for this Identifier.
     */
    public IdentifierInfo info() {
        IdentifierInfo info = getClass().getAnnotation(IdentifierInfo.class);
        if (info == null) {
            throw new IllegalStateException("@IdentifierInfo annotation is missing from " + getClass().getSimpleName());
        }
        return info;
    }

    /**
     * Gets the list of added BytecodeParsers.
     *
     * @return The list of added BytecodeParsers.
     */
    public List<BytecodeParser> parsers() {
        return parsers;
    }

    /**
     * Gets the expected values count.
     *
     * @return The amount of values expected.
     */
    public int expectedValueCount() {
        int count = 0;
        for (BytecodeParser parser : parsers()) {
            count += parser.expectedValues.size();
        }
        return count;
    }

    /**
     * Gets the found values count.
     *
     * @return The amount of values found.
     */
    public int foundValueCount() {
        int count = 0;
        for (BytecodeParser parser : parsers()) {
            count += parser.foundValues.size();
        }
        return count;
    }

    /**
     * Gets a list of all missing values.
     *
     * @return A list of all missing values.
     */
    public List<String> missingValues() {
        List<String> missing = new ArrayList<>();
        for (BytecodeParser parser : parsers()) {
            parser.expectedValues.forEach((hook) -> {
                if (!parser.foundValues.containsKey(hook)) {
                    missing.add(parser.info().name() + "#" + hook);
                }
            });
        }
        return missing;
    }

    /**
     * Sets this Identifier's IdentifierLogger.
     *
     * @param logger The logger to set.
     */
    public void setLogger(IdentifierLogger logger) {
        this.logger = logger;
    }

    /**
     * A method that will execute before analysis, this should be overridden.
     *
     * @param classes The classes to parse.
     */
    public void before(Map<String, ClassFactory> classes) {

    }

    /**
     * A method that will execute after analysis, this should be overridden.
     *
     * @param classes The classes to parse.
     */
    public void after(Map<String, ClassFactory> classes) {

    }

    @Override
    public void run() {
        populateParsers(parsers);
        before(classes);
        parsers.forEach(parser -> {
            for (ClassFactory factory : classes.values()) {
                if (parser.test(factory)) {
                    break;
                }
            }
        });
        parsers.forEach(parser -> parser.parse(classes));
        after(classes);
    }

    /**
     * Gets the IdentifierLogger for this Identifier.
     *
     * @return The IdentifierLogger for this Identifier.
     */
    public IdentifierLogger logger() {
        return logger;
    }

    /**
     * Prints out this Identifier's information using its IdentifierLogger.
     */
    public void log() {
        logger.print(this);
    }
}
