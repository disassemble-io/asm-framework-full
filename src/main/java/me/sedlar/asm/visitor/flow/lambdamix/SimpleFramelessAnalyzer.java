package me.sedlar.asm.visitor.flow.lambdamix;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.List;

/**
 * Specialized lite version of {@link FramelessAnalyzer}.
 * No processing of Subroutines. May be used for methods without JSR/RET instructions.
 *
 * @author lambdamix
 */
public class SimpleFramelessAnalyzer extends FramelessAnalyzer {

    @Override
    protected void findSubroutine(int insn, Subroutine sub, List<AbstractInsnNode> calls) throws AnalyzerException {
    }

    @Override
    protected void merge(int insn, Subroutine subroutine) throws AnalyzerException {
        if (!wasQueued[insn]) {
            wasQueued[insn] = true;
            if (!queued[insn]) {
                queued[insn] = true;
                queue[top++] = insn;
            }
        }
    }

    @Override
    protected void merge(int insn, Subroutine subroutineBeforeJSR, boolean[] access) throws AnalyzerException {
        if (!wasQueued[insn]) {
            wasQueued[insn] = true;
            if (!queued[insn]) {
                queued[insn] = true;
                queue[top++] = insn;
            }
        }
    }
}