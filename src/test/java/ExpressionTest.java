import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.JarArchive;
import io.disassemble.asm.visitor.expr.ExprTree;
import io.disassemble.asm.visitor.expr.MultiExprTreeVisitor;
import org.junit.BeforeClass;
import org.junit.Test;
import visitor.EuclideanVisitor;
import visitor.ParameterVisitor;

import java.io.IOException;
import java.util.*;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class ExpressionTest {

    private static final String TEST_CLASS_NAME = "";//"Sample";
    private static final String TEST_JAR = "./src/test/excluded-java/res/jars/116.jar";

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

        EuclideanVisitor euclid = new EuclideanVisitor();
        ParameterVisitor param = new ParameterVisitor();
        MultiExprTreeVisitor visitor = new MultiExprTreeVisitor(Arrays.asList(euclid, param));

        start = System.nanoTime();
        trees.values().forEach(exprTrees -> exprTrees.forEach(tree -> tree.accept(visitor)));
        end = System.nanoTime();

        System.out.printf("dispatched visitors in %.4f seconds", (end - start) / 1e9);

        System.out.printf("visited %sD/%sE mults\n", euclid.decoders().size(), euclid.encoders().size());

        start = System.nanoTime();
        Map<String, Number> matched = euclid.match();
        end = System.nanoTime();

        System.out.printf("matched %s multipliers in %.4f seconds\n", matched.size(), (end - start) / 1e9);
        
        System.out.printf("removed %s unused parameters\n", param.removed());

//        new TreeSet<>(matched.keySet())
//                .forEach(key -> System.out.println(key + " * " + matched.get(key)));

    }
}
