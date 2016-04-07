import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.flow.FlowQuery;
import io.disassemble.asm.visitor.flow.FlowQueryResult;
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
                        cfg.execution().print();
                        List<FlowQueryResult> results = cfg.execution().query(
                                new FlowQuery()
                                        .stmtIncrement()
                                        .stmtIncrement()
                                        .stmtAdd()
                                        .stmtPush()
                                        .stmtIf()
                                        .branchTrue()
                                        .stmtLoad()
                                        .stmtLoad()
                                        .stmtLoad()
                        );
                        System.out.println("query results: " + results.size());
//                        cfg.renderToSVG(new File("./src/test/excluded-java/out/" + cm.key() + ".svg"));
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
