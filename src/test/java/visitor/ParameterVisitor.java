package visitor;

import io.disassemble.asm.visitor.expr.ExprTree;
import io.disassemble.asm.visitor.expr.ExprTreeVisitor;
import io.disassemble.asm.visitor.expr.node.VarLoadExpr;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Tyler Sedlar
 * @since 6/21/16
 */
public class ParameterVisitor extends ExprTreeVisitor {

    private int removed = 0;
    private Deque<Integer> unused = new ArrayDeque<>();

    public int removed() {
        return removed;
    }

    @Override
    public void visitStart(ExprTree tree) {
        int offset = ((tree.method().access() & Opcodes.ACC_STATIC) > 0 ? 0 : 1);
        Type[] types = Type.getArgumentTypes(tree.method().desc());
        for (int i = 0; i < types.length; i++) {
            unused.add(i + offset);
        }
    }

    @Override
    public void visitVarLoadExpr(VarLoadExpr expr) {
        unused.remove(expr.var());
    }

    @Override
    public void visitEnd(ExprTree tree) {
        removed += unused.size();
        // itr unused and regenerate the new method descriptor
        unused.clear();
    }
}
