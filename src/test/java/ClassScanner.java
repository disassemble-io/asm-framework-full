import me.sedlar.asm.ClassFactory;
import me.sedlar.asm.ClassMethod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 2/25/2016
 */
public class ClassScanner {

    public static void scanClassPath(Predicate<ClassNode> predicate, Consumer<ClassMethod> consumer) {
        String list = System.getProperty("java.class.path");
        for (String path : list.split(File.pathSeparator)) {
            File file = new File(path);
            if (file.isDirectory()) {
                scanDirectory(file, predicate, consumer);
            } else if (path.endsWith(".class")) {
                scanClassFile(path, predicate, consumer);
            }
        }
    }

    public static void scanDirectory(File directory, Predicate<ClassNode> predicate, Consumer<ClassMethod> consumer) {
        for (String entry : directory.list()) {
            String path = directory.getPath() + File.separator + entry;
            File file = new File(path);
            if (file.isDirectory()) {
                scanDirectory(file, predicate, consumer);
            } else if (file.isFile() && path.endsWith(".class")) {
                scanClassFile(path, predicate, consumer);
            }
        }
    }

    public static void scanClassFile(String path, Predicate<ClassNode> predicate, Consumer<ClassMethod> consumer) {
        try (InputStream input = new FileInputStream(path)) {
            scanInputStream(input, predicate, consumer);
        } catch (IOException e) {
            System.out.println("File was not found: " + path);
        }
    }

    public static void scanInputStream(InputStream is, Predicate<ClassNode> predicate, Consumer<ClassMethod> consumer) {
        try {
            ClassReader reader = new ClassReader(is);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_DEBUG);
            if (!predicate.test(node)) {
                return;
            }
            ClassFactory factory = new ClassFactory(node);
            List methods = node.methods;
            for (Object methodObject : methods) {
                ClassMethod method = new ClassMethod(factory, (MethodNode) methodObject);
                consumer.accept(method);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
