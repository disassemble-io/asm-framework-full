import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.visitor.expr.ExpressionVisitor;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class ExpressionTest {

    private static final String TEST_CLASS_NAME = "Sample";

    private static final Map<String, ClassFactory> classes = new HashMap<>();

    @BeforeClass
    public static void setup() {
        ClassScanner.scanClassPath(
                cn -> cn.name.equals(TEST_CLASS_NAME),
                cm -> classes.put(cm.owner.name(), cm.owner)
        );
    }

    /**
     * TODO:
     * - use this to build trees for heritage -- include (Deque<BasicExpr> heritage) in BasicExpr.
     * - replace /indent/ param with (BasicExpr parent)
     */
    private void handleExpr(Iterator<BasicExpr> itr, BasicExpr expr, String indent) {
        System.out.println(indent + Assembly.toString(expr.insn) + " - pulls from " + expr.proceeding() + " slots");
        int proceeding = expr.proceeding();
        for (int i = 0; i < proceeding; i++) {
            handleExpr(itr, itr.next(), indent + "  ");
        }
    }

    @Test
    public void testExpression() {
        Map<String, List<BasicExpr>> expressions = new HashMap<>();
        ExpressionVisitor visitor = new ExpressionVisitor() {
            public void visitExpr(BasicExpr expr) {
                String key = method.key();
                if (!expressions.containsKey(key)) {
                    expressions.put(key, new ArrayList<>());
                }
                expressions.get(key).add(expr);
            }
        };
        long start = System.nanoTime();
        classes.values().forEach(factory -> {
            for (ClassMethod method : factory.methods) {
                if (method.key().equals("Sample.t(I)V")) {
                    method.accept(visitor);
                    System.out.println(method.key() + ":");
                    List<BasicExpr> exprs = expressions.get(method.key());
                    Collections.reverse(exprs);
                    Iterator<BasicExpr> itr = exprs.iterator();
                    while (itr.hasNext()) {
                        handleExpr(itr, itr.next(), "");
                    }
                    // TODO: fetch /only/ root exprs + call Collections#reverse them again to maintain previous order.
                }
            }
        });
        long end = System.nanoTime();
        System.out.printf("%.4f seconds\n", (end - start) / 1e9);
    }
}
