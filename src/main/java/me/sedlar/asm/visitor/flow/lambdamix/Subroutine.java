package me.sedlar.asm.visitor.flow.lambdamix;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lambdamix
 */
public class Subroutine {

    protected LabelNode start;
    protected boolean[] access;
    protected List<JumpInsnNode> callers;

    private Subroutine() {
    }

    Subroutine(LabelNode start, int maxLocals, JumpInsnNode caller) {
        this.start = start;
        this.access = new boolean[maxLocals];
        this.callers = new ArrayList<>();
        callers.add(caller);
    }

    public Subroutine copy() {
        Subroutine result = new Subroutine();
        result.start = start;
        result.access = new boolean[access.length];
        System.arraycopy(access, 0, result.access, 0, access.length);
        result.callers = new ArrayList<>(callers);
        return result;
    }

    public boolean merge(Subroutine subroutine) throws AnalyzerException {
        boolean changes = false;
        for (int i = 0; i < access.length; ++i) {
            if (subroutine.access[i] && !access[i]) {
                access[i] = true;
                changes = true;
            }
        }
        if (subroutine.start == start) {
            for (int i = 0; i < subroutine.callers.size(); ++i) {
                JumpInsnNode caller = subroutine.callers.get(i);
                if (!callers.contains(caller)) {
                    callers.add(caller);
                    changes = true;
                }
            }
        }
        return changes;
    }
}