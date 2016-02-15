package me.sedlar.asm.updater;

import me.sedlar.asm.ClassFactory;
import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.util.Query;
import me.sedlar.asm.visitor.flow.FlowQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public abstract class Analyzer implements Predicate<ClassFactory> {

    protected ClassFactory factory;
    protected final List<Query> queries = new ArrayList<>();

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

    public void analyze(Map<String, ClassFactory> classes) {
        populateQueries(queries);
        List<FlowQuery> flowQueries = queries.stream()
                .filter(query -> query instanceof FlowQuery)
                .map(query -> (FlowQuery) query)
                .collect(Collectors.toList());
        System.out.println("flowQueries: " + flowQueries.size());
        classes.values().forEach(factory -> {
            for (ClassMethod method : factory.methods) {
                method.cfg().ifPresent(cfg -> {
                    flowQueries.stream().filter(query -> !query.locked()).forEach(query -> {
                        query.find(cfg).ifPresent(results -> results.forEach(result -> {
                            System.out.println(result);
                        }));
                    });
                });
            }
        });
    }
}
