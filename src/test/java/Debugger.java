import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.util.Assembly;
import me.sedlar.asm.visitor.flow.FlowQuery;
import me.sedlar.asm.visitor.flow.FlowQueryResult;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
public class Debugger implements Opcodes {

    private static final String TEST_CLASS_NAME = "Sample";

    private static final Map<String, ClassMethod> methods = new HashMap<>();

    @BeforeClass
    public static void setup() {
        ClassScanner.scanClassPath(
            cn -> cn.name.equals(TEST_CLASS_NAME),
            cm -> methods.put(cm.key(), cm)
        );
    }

    @Test
    public void testControlFlowGraph() {
        methods.values().forEach(cm -> {
            if (cm.name().contains("<")) {
                return;
            }
            if (cm.name().contains("cfg")) {
                try {
                    long start = System.nanoTime();
                    cm.cfg().ifPresent(cfg -> {
                        cfg.printBasicBlocks();
//                        cfg.execution().printTree();
//                        List<FlowQueryResult> results = cfg.execution().query(
//                                new FlowQuery()
//                                        .stmtIncrement()
//                                        .stmtIncrement()
//                                        .stmtAdd()
//                                        .stmtPush()
//                                        .doesNotLoop()
//                        );
//                        System.out.println("query results: " + results.size());
//                        results.forEach(result -> result.findInstruction("root-loader")
//                                .ifPresent(insn -> System.out.println("root-loader: " + Assembly.toString(insn))));
//                        cfg.execution().printTree();
//                        BufferedImage image = cfg.dotImage(null, null);
//                        try {
//                            ImageIO.write(image, "png", new File("./src/test/excluded-java/out/" + cm.key() + ".png"));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    });
                    long end = System.nanoTime();
                    System.out.println(String.format("took: %.2f seconds", (end - start) / 1e9));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
