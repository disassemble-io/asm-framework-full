import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.visitor.expr.ExprTree;
import io.disassemble.asm.visitor.expr.ExprTreeBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Tyler Sedlar
 * @since 6/18/16
 */
public class DecompileTest {

    private static final String TEST_CLASS_NAME = "Sample";

    private ConcurrentMap<String, ClassFactory> classes;

    @Before
    public void setup() {
//        System.setProperty(ExprTree.VERBOSE_EXPRESSION_TREE, Boolean.toString(true));
        if (!TEST_CLASS_NAME.isEmpty()) {
            classes = new ConcurrentHashMap<>();
            ClassScanner.scanClassPath(
                    cn -> cn.name.equals(TEST_CLASS_NAME),
                    cm -> {
                        if (!classes.containsKey(cm.owner.name())) {
                            classes.put(cm.owner.name(), cm.owner);
                        }
                    }
            );
        }
    }

    @Test
    public void test() {
        Map<String, Deque<ExprTree>> trees = new HashMap<>();
        long start = System.nanoTime();
        trees.putAll(ExprTreeBuilder.buildAll(classes));
        long end = System.nanoTime();
        System.out.printf("trees built in %.4f seconds\n", (end - start) / 1e9);
        trees.get("Sample").forEach(tree -> {
            if (tree.method().key().equals("Sample.u(I)I")) {
                System.out.println("===============================");
                System.out.println(tree.method().key());
                System.out.println("===============================");
                tree.print();
                System.out.println("===============================");
                System.out.println();
            }
        });
    }
}
