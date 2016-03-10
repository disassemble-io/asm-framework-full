package io.disassemble.asm.program.log;

import io.disassemble.asm.program.BytecodeParser;
import io.disassemble.asm.program.Identifier;

import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public abstract class IdentifierLogger {

    public abstract void printHeader(Identifier updater);
    public abstract void printParser(BytecodeParser parser);
    public abstract void printFooter(Identifier updater);

    public void printParsers(List<BytecodeParser> parsers) {
        parsers.forEach(this::printParser);
    }

    public void print(Identifier updater) {
        printHeader(updater);
        printParsers(updater.parsers());
        printFooter(updater);
    }
}
