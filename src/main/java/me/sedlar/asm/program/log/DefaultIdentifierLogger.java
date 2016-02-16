package me.sedlar.asm.program.log;

import me.sedlar.asm.program.BytecodeParser;
import me.sedlar.asm.program.Identifier;
import me.sedlar.asm.program.IdentifierInfo;
import me.sedlar.asm.util.Assembly;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public class DefaultIdentifierLogger extends IdentifierLogger {

    @Override
    public void printHeader(Identifier updater) {
        IdentifierInfo info = updater.info();
        System.out.println("- " + info.name() + " by " + info.author() + " -");
    }

    @Override
    public void printParser(BytecodeParser parser) {
        String factoryName = (parser.factory() != null ? parser.factory().name() : null);
        System.out.println("* " + parser.info().name() + " as '" + factoryName + "'");
        parser.foundValues.forEach((name, instructions) -> {
            System.out.println("  * " + name + ":");
            instructions.forEach(insn -> System.out.println("    ^ " + Assembly.toString(insn)));
        });
    }

    @Override
    public void printFooter(Identifier updater) {

    }
}
