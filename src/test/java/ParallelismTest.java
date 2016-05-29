import io.disassemble.asm.util.JarArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christopher Carpenter
 */
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParallelismTest {
    private static final File TEST_JAR = new File(ParallelismTest.class.getResource("SampleArchive.jar").getFile());
    private long parallelismThreshold;

    public ParallelismTest(long parallelismThreshold) {
        this.parallelismThreshold = parallelismThreshold;
    }

    @Parameterized.Parameters(name = "parallelismThreshold: {0}")
    public static List primeNumbers() {
        return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, Long.MAX_VALUE);
    }

    @BeforeClass
    public static void warmUp() throws IOException {
        //TODO use JMH for benchmarking instead, this isn't what JUnit was designed for.
        //System.out.println("Warming up the JIT");
        for (int iteration = 0; iteration < 25; ++iteration) {
            new JarArchive(TEST_JAR).build(true);
            new JarArchive(TEST_JAR).build(false);
            new JarArchive(TEST_JAR).build(iteration);
        }
    }

    @Test
    public void test() throws IOException {
        JarArchive ja = new JarArchive(TEST_JAR);
        ja.build(parallelismThreshold);
        Assert.assertTrue(!ja.classes().isEmpty() && !ja.resources().isEmpty());
        //System.out.println("Built " + ja.classes().size() + " classes and " + ja.resources().size() + " resources with a parallelism threshold of " + parallelismThreshold + ".");
    }
}
