package me.sedlar.asm.visitor.stack;

import me.sedlar.asm.ClassMethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 3/9/15
 */
public abstract class FrameVisitor extends ClassMethodVisitor {

    private final Map<String, List<StackFrame>> frames = new HashMap<>();
    private final StackInterpreter interpreter;
    private final Analyzer analyzer;

    public FrameVisitor(StackInterpreter interpreter, Analyzer analyzer) {
        this.interpreter = interpreter;
        this.analyzer = analyzer;
    }

    public abstract void visitFrame(StackFrame frame);

    @Override
    public void visitCode() {
        super.visitCode();
        try {
            List<StackFrame> cached = null;
            if (frames.containsKey(method.key())) {
                cached = frames.get(method.key());
            } else {
                Frame[] frames = analyzer.analyze(method.owner.name(), method.method);
                if (frames != null && frames.length > 0) {
                    List<List<AbstractInsnNode>> frameInstructions = new LinkedList<>();
                    List<StackFrame> stackFrames = new LinkedList<>();
                    for (Frame frame : frames) {
                        if (frame == null) {
                            continue;
                        }
                        int locals = frame.getLocals();
                        if (locals > 0) {
                            for (int i = 0; i < locals; i++) {
                                BasicValue val = (BasicValue) frame.getLocal(i);
                                if (val != null && interpreter.values.containsKey(val)) {
                                    List<AbstractInsnNode> list = interpreter.values.get(val);
                                    if (!frameInstructions.contains(list)) {
                                        stackFrames.add(new StackFrame(interpreter, frame, new StackValue(val, list)));
                                        frameInstructions.add(list);
                                    }
                                }
                            }
                        }
                        this.frames.put(method.key(), (cached = stackFrames));
                    }
                } else {
                    this.frames.put(method.key(), null);
                }
            }
            if (cached == null)
                return;
            cached.forEach(this::visitFrame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitEnd() {
        interpreter.values.clear();
    }

    public void clean() {
        interpreter.values.clear();
        frames.clear();
    }

    @Override
    public String toString() {
        int size = 0;
        for (List<StackFrame> frames : this.frames.values()) {
            size += frames.size();
        }
        return Integer.toString(size);
    }
}
