package io.disassemble.asm.util;

import com.linkedin.parseq.MultiException;
import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassFactoryVisitor;
import io.disassemble.asm.ClassMethodVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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
public class JarArchive {
    private final ConcurrentHashMap<String, ClassFactory> classes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, byte[]> resources = new ConcurrentHashMap<>();
    private final File file;
    private boolean built = false;

    /**
     * Constructs a {@code JarArchive} using the specified input file.
     *
     * @param file the {@code File} to build the JarArchive from.
     */
    public JarArchive(File file) {
        this.file = file;
    }

    /**
     * Completely reads an open input stream and then closes it.
     *
     * @param in The InputStream to be read
     * @return a non-null {@code byte[]} containing the results the now closed <@code InputStream>
     */
    private static byte[] readInputStream(InputStream in) throws IOException {
        try (ReadableByteChannel inChannel = Channels.newChannel(in)) {
            //ByteArrayOutputStreams don't need to be closed.
            /*
             TODO
             The default initial capacity of a ByteArrayOutputStream is only 32.
             We need to set an initial capacity becuause when it's exceeded it only doubles.
             That means it'll resize a LOT on large classes.
             I recommend we check if we have an InputStream with a reliable #available method.
             If so, we should use an initial capacity based on that.
             Otherwise, I recommend something along the lines of Math.min(4096, Math.max(32, in.available()));
             */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            /*
            TODO
            Is writing to a WriteByteChannel that wraps a ByteArrayOutputStream more efficient?
            I don't see how it could be. It seems like it would just add overhead. I understand that
            using NIO for reading would yield better results.
            */
            try (WritableByteChannel outChannel = Channels.newChannel(baos)) {
                /*
                TODO
                Benchmark if 4096 is optimal, allocating 8192 my be more efficient because that's
                the default transfer size of ReadableByteChannelImpl. However, if {@code in} is
                an instance of FileInputStream the default transfer size may be different
                because Channels.newChannel(InputStream) will instead return an implementation of
                FileChannelImpl.
                */
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                while (inChannel.read(buffer) != -1) {
                    buffer.flip();
                    outChannel.write(buffer);
                    buffer.compact();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    outChannel.write(buffer);
                }
                return baos.toByteArray();
            }
        }
    }

    public ConcurrentMap<String, ClassFactory> classes() {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before its classes can be retrieved.");
        }
        return classes;
    }

    public ConcurrentMap<String, byte[]> resources() {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before its resources can be retrieved.");
        }
        return resources;
    }

    /**
     * Dispatches the given visitor to all the loaded classes.
     *
     * @param cfv The visitor to dispatch.
     */
    public void dispatch(ClassFactoryVisitor cfv) {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before visitors can be dispatched.");
        }
        for (ClassFactory factory : classes.values()) {
            factory.accept(cfv);
        }
    }

    /**
     * Dispatches the given visitor to all the loaded methods.
     *
     * @param cmv The visitor to dispatch.
     */
    public void dispatch(ClassMethodVisitor cmv) {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before visitors can be dispatched.");
        }
        for (ClassFactory factory : classes.values()) {
            factory.dispatch(cmv);
            if (cmv.locked()) {
                return;
            }
        }
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
     * Gets the build state, that is whether or not this JarArchive has been built.
     *
     * @return true if this JarArchive has been built.
     */
    public boolean built() {
        return built;
    }

    /**
     * Builds the class and resource maps using parallelism.
     *
     * @return The amount of milliseconds it took to build the classes and resources
     * @throws IOException if an error occurs while locating, reading, or parsing the file
     */
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
                        reader.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
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
    public void write() throws IOException {
        write(file, ClassWriter.COMPUTE_MAXS);
    }

    /**
     * Clears the contents of the classes and resources and resets {@code built} to false.
     */
    public void reset() {
        classes.clear();
        resources.clear();
        built = false;
    }
}
