package io.disassemble.asm.visitor.stack;

import io.disassemble.asm.util.StringMatcher;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * @author Tyler Sedlar
 * @since 3/10/15
 */
public class StackFrame {

    private final StackInterpreter interpreter;
    private final Frame frame;
    private final StackValue top;

    public StackFrame(StackInterpreter interpreter, Frame frame, StackValue top) {
        this.interpreter = interpreter;
        this.frame = frame;
        this.top = top;
    }

    public StackValue top() {
        return top;
    }

    public int getLocals() {
        return frame.getLocals();
    }

    public StackValue getLocal(int i) throws IndexOutOfBoundsException {
        BasicValue val = (BasicValue) frame.getLocal(i);
        return new StackValue(val, interpreter.values.get(val));
    }

    public int getStackSize() {
        return frame.getStackSize();
    }

    public StackValue getStack(int i) throws IndexOutOfBoundsException {
        BasicValue val = (BasicValue) frame.getStack(i);
        return new StackValue(val, interpreter.values.get(val));
    }

    public StackValue pop() {
        BasicValue val = (BasicValue) frame.pop();
        return new StackValue(val, interpreter.values.get(val));
    }

    @Override
    public int hashCode() {
        return frame.hashCode();
    }

    public String localInfo() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getLocals(); ++i) {
            builder.append(getLocal(i).toString());
        }
        return builder.toString();
    }

    public boolean localInfoMatches(String checker) {
        return checker.isEmpty() || StringMatcher.matches(checker, localInfo());
    }

    public String stackInfo() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getStackSize(); ++i) {
            builder.append(getStack(i).toString());
        }
        return builder.toString();
    }

    public String info() {
        return localInfo() + ' ' + stackInfo();
    }

    @Override
    public String toString() {
        return info();
    }
}
