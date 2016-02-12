import me.sedlar.asm.ClassFactory;
import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.pattern.nano.composite.BasicChainedSetterPattern;
import me.sedlar.asm.pattern.nano.composite.BasicSetterPattern;
import me.sedlar.asm.pattern.nano.composite.CompositePattern;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
public class Debugger {

    private static final String TEST_CLASS_NAME = "Sample";
    private static final CompositePattern[] COMPOSITE_PATTERNS = {
            new BasicSetterPattern(), new BasicChainedSetterPattern()
    };

    private static final Map<String, ClassMethod> methods = new HashMap<>();

    @Test
    public void testNanoPatterns() {
        methods.values().forEach(cm -> {
            if (cm.name().contains("<")) {
                return;
            }
            System.out.println(cm.key());
            System.out.println("  SIMPLE_NANO_PATTERNS: ");
            cm.findSimpleNanoPatterns().forEach(patternName -> System.out.println("    " + patternName));
            System.out.println("  ADVANCED_NANO_PATTERNS: ");
            cm.findAdvancedNanoPatterns().forEach(patternName -> System.out.println("    " + patternName));
            List<CompositePattern> matching = new ArrayList<>();
            for (CompositePattern pattern : COMPOSITE_PATTERNS) {
                if (pattern.matches(cm)) {
                    matching.add(pattern);
                }
            }
            if (!matching.isEmpty()) {
                System.out.println("  MATCHING_COMPOSITE_PATTERNS: ");
                matching.forEach(cp -> System.out.println("    " + cp.getClass().getSimpleName()));
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
