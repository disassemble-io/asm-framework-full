package io.disassemble.asm.visitor.expr.node;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Grep;
import io.disassemble.asm.visitor.expr.ExprExtractor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;

import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LDC;

/**
 * @author Tyler Sedlar
 * @since 6/18/16
 */
public class MethodExpr extends MemberExpr<MethodInsnNode> {

    public MethodExpr(ClassMethod method, MethodInsnNode insn, int type) {
        super(method, insn, type);
    }

    @Override
    public String key() {
        return (insn.owner + '.' + insn.name + insn.desc);
    }

    @Override
    public String owner() {
        return insn.owner;
    }

    @Override
    public String name() {
        return insn.name;
    }

    @Override
    public String desc() {
        return insn.desc;
    }

    @Override
    public String decompile() {
        return decompile(true);
    }

    public String decompile(boolean opaque) {
        String[] args = args(opaque);
        return (owner() + '.' + name() + '(' + String.join(", ", (CharSequence[]) args) + ')');
    }

    // This obviously needs to be improved, it's for debugging purposes, currently.
    private boolean hasOpaque() {
        BasicExpr<AbstractInsnNode> expr = children.peekLast();
        return expr.opcode() == LDC && ((LdcInsnNode) expr.insn).cst instanceof Number;
    }

    public String[] args(boolean opaque) {
        if (children.isEmpty()) {
            return new String[0];
        }
        boolean skipFirst = (opcode() != INVOKESTATIC && opcode() != INVOKEDYNAMIC);
        String[] args = new String[skipFirst ? (children.size() - 1) : children.size()];
        boolean stripped = false;
        if (opaque && hasOpaque()) {
            args = new String[args.length - 1];
            stripped = true;
        }
        int idx = 0;
        for (BasicExpr<AbstractInsnNode> child : children) {
            if (skipFirst && child == children.peekFirst()) {
                continue;
            } else if (stripped && (idx + 1) > args.length) {
                continue;
            }
            String val = ExprExtractor.extract(child);
            args[idx++] = val;
        }
        return args;
    }

    public String[] args() {
        return args(true);
    }
}
