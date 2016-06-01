import io.disassemble.asm.util.JarArchive;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NanoPatternTest {
    private static final File TEST_JAR = new File(ParallelismTest.class.getResource("SampleArchive.jar").getFile());

    @Test
    public void test() throws IOException {
        JarArchive ja = new JarArchive(TEST_JAR);
        ja.build();
        Map<String, Integer> patterns = new HashMap<>();
        ja.classes().values().forEach(cf -> Arrays.asList(cf.methods).forEach(cm -> {
            for (String name : cm.findSimpleNanoPatterns()) {
                patterns.put(name, patterns.getOrDefault(name, 0) + 1);
            }
            for (String name : cm.findAdvancedNanoPatterns()) {
                patterns.put(name, patterns.getOrDefault(name, 0) + 1);
            }
        }));
        //patterns.forEach((s, integer) -> System.out.println(s + ": " + integer));
        Assert.assertTrue(patterns.get("Chained") == 38);
        Assert.assertTrue(patterns.get("SameName") == 419);
        Assert.assertTrue(patterns.get("TypeManipulator") == 2145);
        Assert.assertTrue(patterns.get("Looping") == 474);
        Assert.assertTrue(patterns.get("LocalWriter") == 5673);
        Assert.assertTrue(patterns.get("FieldWriter") == 1264);
        Assert.assertTrue(patterns.get("ObjectReturn") == 643);
        Assert.assertTrue(patterns.get("ObjectCreator") == 1750);
        Assert.assertTrue(patterns.get("ArrayReader") == 441);
        Assert.assertTrue(patterns.get("DirectlyThrowsException") == 57);
        Assert.assertTrue(patterns.get("NoReturn") == 940);
        Assert.assertTrue(patterns.get("StraightLine") == 1109);
        Assert.assertTrue(patterns.get("SpecifiesException") == 105);
        Assert.assertTrue(patterns.get("NoParameters") == 774);
        Assert.assertTrue(patterns.get("PrimitiveReturn") == 402);
        Assert.assertTrue(patterns.get("ArrayCreator") == 545);
        Assert.assertTrue(patterns.get("Leaf") == 479);
        Assert.assertTrue(patterns.get("ArrayWriter") == 2223);
        Assert.assertTrue(patterns.get("Recursive") == 89);
        Assert.assertTrue(patterns.get("FieldReader") == 5133);
        Assert.assertTrue(patterns.get("LocalReader") == 23134);
    }
}
