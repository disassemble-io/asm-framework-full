import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.JarArchive;
import io.disassemble.asm.visitor.expr.ExprTree;
import io.disassemble.asm.visitor.expr.ExprTreeBuilder;
import io.disassemble.asm.visitor.expr.MultiExprTreeVisitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visitor.EuclideanVisitor;
import visitor.ParameterVisitor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 */
public class ExpressionTest {

    private static final String TEST_CLASS_NAME = "";//"Sample";
    private static final String TEST_JAR = "./src/test/excluded-java/res/jars/117.jar";

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
        if (!TEST_JAR.isEmpty()) {
            JarArchive archive = new JarArchive(TEST_JAR);
            try {
                archive.build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            classes = archive.classes();
        }
    }

    @Test
    public void testExpression() {
        EuclideanVisitor euclid = new EuclideanVisitor();
        Map<String, Number> multipliers = new HashMap<>();
        long start = System.nanoTime();
        classes.values().parallelStream()
                .forEach(factory -> Arrays.asList(factory.methods).parallelStream()
                        .forEach(cm -> cm.tree(false).ifPresent(tree -> tree.accept(euclid))));
        multipliers.putAll(euclid.match());
        long end = System.nanoTime();
        System.out.printf("matched %s multipliers in %.4f seconds\n", multipliers.size(), (end - start) / 1e9);
        System.out.println("!@#!@#!@# " + multipliers.get("p.gq"));
        System.out.println("!@#!@#!@# " + multipliers.get("em.s"));

    }
}
