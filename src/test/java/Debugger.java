import me.sedlar.asm.ClassFactory;
import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.util.Assembly;
import me.sedlar.asm.visitor.flow.FlowQuery;
import me.sedlar.asm.visitor.flow.FlowQueryResult;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        scanClassPath();
    }

    private static void scanClassPath() {
        String list = System.getProperty("java.class.path");
        for (String path : list.split(File.pathSeparator)) {
            File file = new File(path);
            if (file.isDirectory()) {
                scanDirectory(file);
            } else if (path.endsWith(".class")) {
                scanClassFile(path);
            }
        }
    }

    private static void scanDirectory(File directory) {
        for (String entry : directory.list()) {
            String path = directory.getPath() + File.separator + entry;
            File file = new File(path);
            if (file.isDirectory()) {
                scanDirectory(file);
            } else if (file.isFile() && path.endsWith(".class")) {
                scanClassFile(path);
            }
        }
    }

    private static void scanClassFile(String path) {
        try (InputStream input = new FileInputStream(path)) {
            scanInputStream(input);
        } catch (IOException e) {
            System.out.println("File was not found: " + path);
        }
    }

    private static void scanInputStream(InputStream is) {
        try {
            ClassReader reader = new ClassReader(is);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_DEBUG);
            if (!classNode.name.equals(TEST_CLASS_NAME)) {
                return;
            }
            ClassFactory factory = new ClassFactory(classNode);
            List methods = classNode.methods;
            for (Object methodObject : methods) {
                ClassMethod method = new ClassMethod(factory, (MethodNode) methodObject);
                Debugger.methods.put(method.key(), method);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        List<FlowQueryResult> results = cfg.execution().query(
                            new FlowQuery()
                                .stmtLoad().name("root-loader")
                                .stmtIf().dist(1).branch()
                                .stmtIncrement()
                                .stmtIf().dist(5).branch()
                                .stmtStore()
                                .stmtLoad()
                        );
                        System.out.println("query results: " + results.size());
                        results.forEach(result -> result.findInstruction("root-loader")
                            .ifPresent(insn -> System.out.println("root-loader: " + Assembly.toString(insn))));
                        cfg.execution().printTree();
                        BufferedImage image = cfg.dotImage(null, null);
                        try {
                            ImageIO.write(image, "png", new File("./src/test/excluded-java/out/" + cm.key() + ".png"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
