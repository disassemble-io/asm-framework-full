package me.sedlar.asm.program;

import me.sedlar.asm.ClassFactory;
import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.util.Query;
import me.sedlar.asm.visitor.flow.FlowQuery;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public abstract class BytecodeParser implements Predicate<ClassFactory> {

    public final List<String> expectedValues = new ArrayList<>();
    public final Map<String, List<AbstractInsnNode>> foundValues = new HashMap<>();
    protected final List<Query> queries = new ArrayList<>();
    protected ClassFactory factory;

    public abstract boolean accept(ClassFactory factory);

    public abstract void populateQueries(List<Query> queries);

    @Override
    public final boolean test(ClassFactory factory) {
        if (accept(factory)) {
            this.factory = factory;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the ClassFactory that this BytecodeParser accepted.
     *
     * @return The ClassFactory this BytecodeParser accepted.
     */
    public ClassFactory factory() {
        return factory;
    }

    /**
     * Obtains the ParserInfo annotation for this BytecodeParser.
     *
     * @return The ParserInfo annotation for this BytecodeParser.
     */
    public ParserInfo info() {
        ParserInfo info = getClass().getAnnotation(ParserInfo.class);
        if (info == null) {
            throw new IllegalStateException("@ParserInfo annotation is missing from " + getClass().getSimpleName());
        }
        return info;
    }

    /**
     * Parses all classes that match this class' queries.
     *
     * @param classes The classes to parse.
     */
    public void parse(Map<String, ClassFactory> classes) {
        ParserInfo info = info();
        Collections.addAll(expectedValues, info.hooks());
        populateQueries(queries);
        List<FlowQuery> flowQueries = queries.stream()
            .filter(query -> query instanceof FlowQuery)
            .map(query -> (FlowQuery) query)
            .collect(Collectors.toList());
        classes.values().forEach(factory -> {
            for (ClassMethod method : factory.methods) {
                method.cfg()
                    .ifPresent(cfg -> flowQueries.stream()
                        .filter(query -> !query.locked())
                        .forEach(query -> query.find(cfg)
                            .ifPresent(results -> results.forEach(result ->
                                result.namedInstructions().forEach((name, insn) -> {
                                    if (!foundValues.containsKey(name)) {
                                        foundValues.put(name, new ArrayList<>());
                                    }
                                    foundValues.get(name).add(insn);
                                })))));
            }
        });
    }
}
