package io.disassemble.asm;

import com.linkedin.parseq.MultiException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author Tyler Sedlar
 * @author Christopher Carpenter
 * @version 1.1.0
 * @since 2/12/16
 */
public class JarArchive extends Archive {
    private final ConcurrentHashMap<String, ClassFactory> classes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, byte[]> resources = new ConcurrentHashMap<>();
    private final File file;

    /**
     * Constructs a {@code JarArchive} using the specified input file path.
     *
     * @param filePath the path to the {@code File} to build the JarArchive from.
     */
    public JarArchive(String filePath) {
        this(new File(filePath));
    }

    /**
     * Constructs a {@code JarArchive} using the specified input file.
     *
     * @param file the {@code File} to build the JarArchive from.
     */
    public JarArchive(File file) {
        this.file = file;
    }

    @Override
    public ConcurrentMap<String, ClassFactory> classes() {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before its classes can be retrieved.");
        }
        return classes;
    }

    @Override
    public ConcurrentMap<String, byte[]> resources() {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before its resources can be retrieved.");
        }
        return resources;
    }

    /**
     * The jar archive file
     *
     * @return The jar archive file.
     */
    public File file() {
        return file;
    }

    /**
     * Builds the class and resource maps using parallelism.
     *
     * @return The amount of milliseconds it took to build the classes and resources
     * @throws IOException if an error occurs while locating, reading, or parsing the file
     */
    @Override
    public long build() throws IOException {
        return build(true);
    }

    /**
     * Builds the class and resource maps using a parallelismThreshold of 1 if {@code parallel}, otherwise Long.MAX_VALUE
     *
     * @param parallel a boolean that decides if the classes and resources should be built in parallel
     * @return The amount of milliseconds it took to build the classes and resources
     * @throws IOException if an error occurs while locating, reading, or parsing the file
     */
    public long build(boolean parallel) throws IOException {
        return build(parallel ? 1 : Long.MAX_VALUE);
    }

    /**
     * Builds the class and resource maps using the specified {@code parallelismThreshold}
     *
     * @param parallelismThreshold The parallelismThreshold to be used when parsing the file entries
     * @return The amount of milliseconds it took to build the classes and resources
     * @throws IOException if an error occurs while locating, reading, or parsing the file
     */
    public long build(long parallelismThreshold) throws IOException {
        long time = System.currentTimeMillis();
        if (built()) {
            throw new IllegalStateException("The JarArchive cannot be built more than once.");
        }
        try (JarFile jar = new JarFile(file)) {
            ArrayList<JarEntry> entries = Collections.list(jar.entries());
            ConcurrentHashMap<String, InputStream> entryStreams = new ConcurrentHashMap<>(entries.size());
            for (JarEntry entry : entries) {
                //getInputStream(Entry) is synronized so can't be made to run in parallel
                entryStreams.put(entry.getName(), jar.getInputStream(entry));
            }
            /*
            We're going to catch all IOExceptions, add them to a list, and throw a MultiException afterwards,
            that way nothing gets interrupted and no errors are missed.
             */
            CopyOnWriteArrayList<IOException> forEachExceptions = new CopyOnWriteArrayList<>();
            //Parallelism threshold is the amount of elements required before operations are performed in parallel.
            entryStreams.forEach(parallelismThreshold, (name, input) -> {
                try {
                    if (name.endsWith(".class")) {
                        ClassNode cn = new ClassNode();
                        ClassReader reader = new ClassReader(readInputStream(input));
                        reader.accept(cn, ClassReader.SKIP_FRAMES);
                        classes.put(name.replace(".class", ""), new ClassFactory(cn));
                    } else {
                        resources.put(name, readInputStream(input));
                    }
                } catch (IOException ioe) {
                    forEachExceptions.add(ioe);
                } finally {
                    try {
                        input.close();
                    } catch (IOException ioe) {
                        forEachExceptions.add(ioe);
                    }
                }
            });
            if (!forEachExceptions.isEmpty()) {
                if (forEachExceptions.size() == 1) {
                    throw forEachExceptions.get(0);
                } else {
                    throw new IOException(
                            forEachExceptions.size() + " exceptions occurred while building the class and resource maps.",
                            new MultiException(forEachExceptions));
                }
            }
        }
        built = true;
        return System.currentTimeMillis() - time;
    }

    /**
     * Writes the classes and resources to the specified file using the supplied ClassWriter flags.
     *
     * @param destinationFile The file to write to.
     * @param writerFlags     The ClassWriter flags to use.
     * @throws IOException If an error occurs while manipulating an output stream
     */
    public void write(File destinationFile, int writerFlags) throws IOException {
        if (!built()) {
            throw new IllegalStateException("You cannot write a JarArchive until it has been built.");
        }
        try (JarOutputStream output = new JarOutputStream(new FileOutputStream(destinationFile))) {
            for (Map.Entry<String, ClassFactory> entry : classes.entrySet()) {
                ClassFactory factory = entry.getValue();
                output.putNextEntry(new JarEntry(factory.name().replaceAll("\\.", "/") + ".class"));
                ClassWriter writer = new ClassWriter(writerFlags);
                factory.node.accept(writer);
                output.write(writer.toByteArray());
                output.closeEntry();
            }
            for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
                output.putNextEntry(new JarEntry(entry.getKey()));
                output.write(entry.getValue());
                output.closeEntry();
            }
            output.flush();
        }
    }

    /**
     * Writes the classes and resources back to the original file using the specified ClassWriter flags.
     *
     * @param writerFlags The ClassWriter flags to use.
     * @throws IOException If an error occurs while manipulating an output stream
     */
    public void write(int writerFlags) throws IOException {
        write(file, writerFlags);
    }

    /**
     * Writes the classes and resources back to the original file using the ClassWriter flag COMPUTE_MAXS.
     *
     * @throws IOException If an error occurs while manipulating an output stream
     */
    @Override
    public void write() throws IOException {
        write(file, ClassWriter.COMPUTE_MAXS);
    }
}
