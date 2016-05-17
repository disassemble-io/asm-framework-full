package io.disassemble.asm.program.log;

import io.disassemble.asm.program.BytecodeParser;
import io.disassemble.asm.program.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public abstract class IdentifierLogger {

    private final List<String> header = new ArrayList<>(), footer = new ArrayList<>();

    public abstract void printHeader(Identifier identifier);
    public abstract void printParser(BytecodeParser parser);
    public abstract void printFooter(Identifier identifier);

    public void printParsers(List<BytecodeParser> parsers) {
        parsers.forEach(this::printParser);
    }

    public void insertToHeader(String string) {
        header.add(string);
    }

    public void insertToFooter(String string) {
        footer.add(string);
    }

    private void printList(List<String> list) {
        list.forEach(System.out::println);
    }

    public void print(Identifier identifier) {
        printHeader(identifier);
        printList(header);
        printParsers(identifier.parsers());
        printFooter(identifier);
        printList(footer);
    }
}
