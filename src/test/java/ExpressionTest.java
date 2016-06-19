import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.JarArchive;
import io.disassemble.asm.visitor.expr.ExprTree;
import org.junit.BeforeClass;
import org.junit.Test;
import visitor.EuclideanVisitor;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class ExpressionTest {

    private static final String TEST_CLASS_NAME = "Sample";
    private static final String TEST_JAR = ""; //"./src/test/excluded-java/res/jars/115.jar";

    private static final Map<String, ClassFactory> classes = new HashMap<>();

    @BeforeClass
    public static void setup() {
//        System.setProperty(ExprTree.VERBOSE_EXPRESSION_TREE, Boolean.toString(true));
        if (!TEST_CLASS_NAME.isEmpty()) {
            ClassScanner.scanClassPath(
                    cn -> cn.name.equals(TEST_CLASS_NAME),
                    cm -> {
                        if (!classes.containsKey(cm.owner.name())) {
                            classes.put(cm.owner.name(), cm.owner);
                        }
                    }
            );
        }
        if (!TEST_JAR.isEmpty()) {
            JarArchive archive = new JarArchive(TEST_JAR);
            try {
                archive.build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            classes.putAll(archive.classes());
        }
    }

    @Test
    public void testExpression() {
        Map<String, Deque<ExprTree>> trees = new HashMap<>();
        long start = System.nanoTime();
        trees.putAll(ExprTree.buildAll(classes));
        long end = System.nanoTime();
        System.out.printf("trees built in %.4f seconds\n", (end - start) / 1e9);
        EuclideanVisitor visitor = new EuclideanVisitor();
        start = System.nanoTime();
        trees.values().forEach(exprTrees -> exprTrees.forEach(tree -> tree.accept(visitor)));
        end = System.nanoTime();
        System.out.printf("visited %sD/%sE mults in %.4f seconds\n", visitor.decoders().size(),
                visitor.encoders().size(), (end - start) / 1e9);
        start = System.nanoTime();
        Map<String, Number> matched = visitor.match();
        end = System.nanoTime();
        System.out.printf("found %s multipliers in %.4f seconds\n", matched.size(), (end - start) / 1e9);
        new TreeSet<>(matched.keySet())
                .forEach(key -> System.out.println(key + " * " + matched.get(key)));
    }
}
