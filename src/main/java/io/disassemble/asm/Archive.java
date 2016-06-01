package io.disassemble.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Christopher Carpenter
 */
public abstract class Archive {
    protected boolean built;

    /**
     * Completely reads an open input stream and then closes it.
     *
     * @param in The InputStream to be read
     * @return a non-null {@code byte[]} containing the results the now closed <@code InputStream>
     */
    protected static byte[] readInputStream(InputStream in) throws IOException {
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

    /**
     * Gets the build state, that is whether or not this Archive has been built.
     *
     * @return true if this Archive has been built.
     */
    public boolean built() {
        return built;
    }

    public abstract ConcurrentMap<String, ClassFactory> classes();

    public abstract ConcurrentMap<String, byte[]> resources();

    public abstract long build() throws IOException;

    /**
     * Dispatches the given visitor to all the loaded classes.
     *
     * @param cfv The visitor to dispatch.
     */
    public void dispatch(ClassFactoryVisitor cfv) {
        if (!built()) {
            throw new IllegalStateException("The JarArchive must be built before visitors can be dispatched.");
        }
        for (ClassFactory factory : classes().values()) {
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
        for (ClassFactory factory : classes().values()) {
            factory.dispatch(cmv);
            if (cmv.locked()) {
                return;
            }
        }
    }

    public abstract void write() throws IOException;

    /**
     * Clears the contents of the classes and resources and resets {@code built} to false.
     */
    public void reset() {
        classes().clear();
        resources().clear();
        built = false;
    }

    public void accept(ArchiveVisitor visitor) {
        visitor.archive = this;
        visitor.visit();
        for (ClassFactory cf : classes().values()) {
            visitor.visitClassFactory(cf);
        }
        for (ConcurrentMap.Entry<String, byte[]> resource : resources().entrySet()) {
            visitor.visitResource(resource.getKey(), resource.getValue());
        }
    }
}
