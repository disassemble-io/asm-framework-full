import me.sedlar.asm.ClassFactory;
import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.visitor.flow.ControlFlowGraph;
import me.sedlar.asm.visitor.flow.ExecutionPath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

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
public class Debugger {

    private static final String TEST_CLASS_NAME = "Sample";

    private static final Map<String, ClassMethod> methods = new HashMap<>();

    @Test
    public void testControlFlowGraph() {
        methods.values().forEach(cm -> {
            if (cm.name().contains("<")) {
                return;
            }
            if (cm.name().contains("cfg")) {
                try {
                    System.out.println(cm.key() + ":");
                    ControlFlowGraph cfg = ControlFlowGraph.create(null, cm);
                    ExecutionPath path = ExecutionPath.build(cfg);
                    path.printTree();
//                    System.out.println(cfg.toDot(start.get(), null));
//                    System.out.println();
//                    BufferedImage image = cfg.dotImage(start.get(), null);
//                    ImageIO.write(image, "png", new File("./" + cm.key() + ".png"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

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
}
