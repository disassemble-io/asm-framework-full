package io.disassemble.asm.visitor.expr.grep;

import io.disassemble.asm.visitor.expr.node.MethodExpr;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Tyler Sedlar
 * @since 6/25/16
 */
public class MethodExprGrep extends GrepExpr {

    public MethodExprGrep(String query, Consumer<Map<String, String>> consumer) {
        super(query, MethodExpr.class, consumer);
    }
}
