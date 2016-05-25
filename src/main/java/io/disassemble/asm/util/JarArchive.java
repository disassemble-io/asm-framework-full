package io.disassemble.asm.util;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author Tyler Sedlar
 * @since 2/12/16
 */
public class JarArchive {

    private final Map<String, ClassFactory> nodes = new HashMap<>();
    private final Map<String, byte[]> resources = new HashMap<>();

    private final File file;

    public JarArchive(File file) {
        this.file = file;
    }

    /**
     * Dispatches the given visitor to all the loaded classes.
     *
     * @param cfv The visitor to dispatch.
     */
    public void dispatch(ClassFactoryVisitor cfv) {
        for (ClassFactory factory : nodes.values()) {
            factory.accept(cfv);
        }
    }

    /**
     * Dispatches the given visitor to all the loaded methods.
     *
     * @param cmv The visitor to dispatch.
     */
    public void dispatch(ClassMethodVisitor cmv) {
        for (ClassFactory factory : nodes.values()) {
            factory.dispatch(cmv);
            if (cmv.locked()) {
                return;
            }
        }
    }

    private byte[] inputToBytes(InputStream in) {
        try (ReadableByteChannel inChannel = Channels.newChannel(in)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (WritableByteChannel outChannel = Channels.newChannel(baos)) {
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
        } catch (IOException e) {
            return new byte[0];
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
     * Builds the class map.
     *
     * @return A map of classes built.
     */
    public Map<String, ClassFactory> build() {
        if (!nodes.isEmpty()) {
            return nodes;
        }
        try {
            JarFile jar = new JarFile(file);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    ClassNode cn = new ClassNode();
                    try (InputStream input = jar.getInputStream(entry)) {
                        ClassReader reader = new ClassReader(inputToBytes(input));
                        reader.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                        nodes.put(name.replace(".class", ""), new ClassFactory(cn));
                    }
                } else {
                    if (!name.equals("META-INF/MANIFEST.MF")) {
                        try (InputStream input = jar.getInputStream(entry)) {
                            resources.put(name, inputToBytes(input));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error building classes (" + file.getName() + "): ", e.getCause());
        }
        return nodes;
    }

    /**
     * Writes the classes to the given file.
     *
     * @param target The file to write to.
     */
    public void write(File target) {
        try (JarOutputStream output = new JarOutputStream(new FileOutputStream(target))) {
            for (Map.Entry<String, ClassFactory> entry : build().entrySet()) {
                ClassFactory factory = entry.getValue();
                output.putNextEntry(new JarEntry(factory.name().replaceAll("\\.", "/") + ".class"));
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the classes back out to itself.
     */
    public void write() {
        write(file);
    }

    /**
     * Clears the memory of the classes and resources.
     */
    public void clear() {
        nodes.clear();
        resources.clear();
    }
}
