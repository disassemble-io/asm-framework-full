package io.disassemble.asm.visitor.expr.grep;

import io.disassemble.asm.util.Grep;
import io.disassemble.asm.visitor.expr.ExprTreeVisitor;
import io.disassemble.asm.visitor.expr.node.BasicExpr;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Tyler Sedlar
 * @since 6/25/16
 */
public class GrepExpr extends Grep {

    public final Class<?> type;
    public final Consumer<Map<String, String>> consumer;

    /**
     * Creates a Grep that has constraints on the given type.
     *
     * @param pattern The basic grep pattern to be used.
     * @param type The type of BasicExpr.
     * @param consumer The consumer of mapped values.
     */
    public GrepExpr(String pattern, Class<?> type, Consumer<Map<String, String>> consumer) {
        super(pattern);
        this.type = type;
        this.consumer = consumer;
    }

    /**
     * Creates an ExprTreeVisitor that greps throughout the list of given patterns.
     *
     * @param greps The patterns to be executed.
     * @return An ExprTreeVisitor that greps throughout the list of given patterns.
     */
    public static ExprTreeVisitor createVisitor(List<GrepExpr> greps) {
        return new ExprTreeVisitor() {
            public void visitExpr(BasicExpr expr) {
                String source = expr.decompile();
                greps.forEach(grep -> {
                    if (grep.type.isInstance(expr)) {
                        Map<String, String> map = grep.exec(source);
                        if (map != null) {
                            grep.consumer.accept(map);
                        }
                    }
                });
            }
        };
    }
}
