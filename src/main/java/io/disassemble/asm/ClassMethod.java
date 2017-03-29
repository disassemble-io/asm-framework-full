package io.disassemble.asm;

import io.disassemble.asm.pattern.nano.AdvancedNanoPattern;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import io.disassemble.asm.pattern.nano.calling.Chained;
import io.disassemble.asm.pattern.nano.calling.Leaf;
import io.disassemble.asm.pattern.nano.calling.Recursive;
import io.disassemble.asm.pattern.nano.calling.SameName;
import io.disassemble.asm.pattern.nano.flow.control.DirectlyThrowsException;
import io.disassemble.asm.pattern.nano.flow.control.Looping;
import io.disassemble.asm.pattern.nano.flow.control.StraightLine;
import io.disassemble.asm.pattern.nano.flow.data.*;
import io.disassemble.asm.pattern.nano.oop.FieldReader;
import io.disassemble.asm.pattern.nano.oop.FieldWriter;
import io.disassemble.asm.pattern.nano.oop.ObjectCreator;
import io.disassemble.asm.pattern.nano.oop.TypeManipulator;
import io.disassemble.asm.pattern.nano.structural.*;
import io.disassemble.asm.util.Assembly;
import io.disassemble.asm.visitor.expr.ExprTree;
import io.disassemble.asm.visitor.expr.ExprTreeBuilder;
import io.disassemble.asm.visitor.flow.ControlFlowGraph;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author Tyler Sedlar
 * @author Christopher Carpenter
 * @since 3/8/15
 */
public class ClassMethod {

    private static final SimpleNanoPattern[] SIMPLE_NANO_PATTERNS = {
            new NoParameters(), new NoReturn(), new PrimitiveReturn(), new ClassReturn(), new ArrayReturn(), new Annotated(), new SpecifiesException(), // Structural
            new Chained(), new Recursive(), new SameName(), new Leaf(), // Calling
            new StraightLine(), new Looping(), new DirectlyThrowsException(), // Control Flow
    };

    private static final AdvancedNanoPattern[] ADVANCED_NANO_PATTERNS = {
            new ObjectCreator(), new FieldReader(), new FieldWriter(), new TypeManipulator(), // Object-Oriented
            new LocalReader(), new LocalWriter(), new ArrayCreator(), new ArrayReader(), new ArrayWriter() // Data Flow
    };

    private static ConcurrentMap<String, ClassMethod> CACHED = new ConcurrentHashMap<>();

    public final ClassFactory owner;
    public final MethodNode method;

    private Type[] types;

    private List<String> simpleNanoPatterns, advancedNanoPatterns;

    private ControlFlowGraph cfg;
    private ExprTree tree;

    public ClassMethod(ClassFactory owner, MethodNode method) {
        this.owner = owner;
        this.method = method;
        CACHED.put(key(), this);
    }

    /**
     * Gets the ClassMethod matching the given key, if it is within the cache.
     *
     * @param key The key to match.
     * @return The ClassMethod matching the given key, if it is within the cache.
     */
    public static ClassMethod resolve(String key) {
        return CACHED.get(key);
    }

    /**
     * Calls MethodNode#accept with the given visitor.
     *
     * @param cmv The visitor to call.
     */
    public void accept(ClassMethodVisitor cmv) {
        cmv.method = this;
        method.accept(cmv);
    }

    /**
     * Gets this method's name.
     *
     * @return The name of this method.
     */
    public String name() {
        return method.name;
    }

    /**
     * Sets the name of this method.
     *
     * @param name The name to set this method's name to.
     */
    public void setName(String name) {
        method.name = name;
    }

    /**
     * Gets this method's desc.
     *
     * @return The desc of this method.
     */
    public String desc() {
        return method.desc;
    }

    /**
     * Sets the desc of this method.
     *
     * @param desc The desc to set this method's desc to.
     */
    public void setDescriptor(String desc) {
        types = null;
        method.desc = desc;
    }

    /**
     * Gets this method's access flags.
     *
     * @return The access flags of this method.
     */
    public int access() {
        return method.access;
    }

    /**
     * Sets this method's access flags.
     *
     * @param access The access flags to set this method's access to.
     */
    public void setAccess(int access) {
        method.access = access;
    }

    /**
     * Checks whether this method is non-static.
     *
     * @return true if this method is non-static, otherwise false.
     */
    public boolean local() {
        return (access() & ACC_STATIC) == 0;
    }

    /**
     * Gets the InsnList for this method.
     *
     * @return The InsnList for this method.
     */
    public InsnList instructions() {
        return method.instructions;
    }

    public List<String> exceptions() {
        return method.exceptions;
    }

    /**
     * Gets this method's key label (class.name + "." + method.name + method.desc)
     *
     * @return This method's key label (class.name + "." + method.name + method.desc)
     */
    public String key() {
        return owner.node.name + '.' + method.name + method.desc;
    }

    /**
     * Removes this method from its class.
     */
    public void remove() {
        owner.remove(this);
    }

    /**
     * Gets the amount of instructions matching the given opcode.
     *
     * @param opcode The opcode to match.
     * @return The amount of instructions matching the given opcode.
     */
    public int count(int opcode) {
        return count(insn -> insn.getOpcode() == opcode);
    }

    /**
     * Gets the amount of instructions matching the given predicate.
     *
     * @param predicate The predicate to match.
     * @return The amount of instructions matching the given predicate.
     */
    public int count(Predicate<AbstractInsnNode> predicate) {
        return Assembly.count(instructions(), predicate);
    }

    /**
     * Obtains a method in the given factory class matching the name and descriptor of this method.
     *
     * @param factory The factory to resolve as.
     * @return A method in the given factory class matching the name and descriptor of this method.
     */
    public ClassMethod resolveTo(ClassFactory factory) {
        if (factory == null) {
            return null;
        }
        return factory.findMethod(cm -> cm.name().equals(name()) && cm.desc().equals(desc()));
    }

    /**
     * Gets the methods that call this method.
     *
     * @param classes The ClassFactory map to search.
     * @return The methods that call this method.
     */
    public List<MethodInsnNode> callers(Map<String, ClassFactory> classes) {
        List<MethodInsnNode> callers = new ArrayList<>();
        for (ClassFactory factory : classes.values()) {
            for (ClassMethod method : factory.methods) {
                for (AbstractInsnNode ain : method.instructions().toArray()) {
                    if (ain instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) ain;
                        if ((min.owner + '.' + min.name + min.desc).equals(key())) {
                            callers.add(min);
                        }
                    }
                }
            }
        }
        return callers;
    }

    /**
     * Checks whether this method returns a desc of the class it's in.
     *
     * @return true if this method returns a desc of the class it's in, otherwise false.
     */
    public boolean chained() {
        return local() && desc().endsWith(")L" + owner.name() + ';');
    }

    /**
     * Checks whether this method calls a method matching the given predicate or not.
     *
     * @param predicate The predicate to match.
     * @return true if this method calls a method matching the given predicate, otherwise false.
     */
    public boolean calls(Predicate<MethodInsnNode> predicate) {
        return count(insn -> insn instanceof MethodInsnNode && predicate.test((MethodInsnNode) insn)) > 0;
    }

    /**
     * Gets a list of simple nano-patterns that are used within this method.
     *
     * @param cached Whether to used the cached list from prior lookups or not.
     * @return A list of simple nano-patterns that are used within this method.
     */
    public List<String> findSimpleNanoPatterns(boolean cached) {
        if (cached && simpleNanoPatterns != null) {
            return simpleNanoPatterns;
        }
        List<String> matching = new ArrayList<>();
        for (SimpleNanoPattern pattern : SIMPLE_NANO_PATTERNS) {
            if (pattern.matches(this)) {
                matching.add(pattern.info().name());
            }
        }
        return (simpleNanoPatterns = matching);
    }

    /**
     * Gets a list of simple nano-patterns that are used within this method.
     *
     * @return A list of simple nano-patterns that are used within this method.
     */
    public List<String> findSimpleNanoPatterns() {
        return findSimpleNanoPatterns(true);
    }

    /**
     * Checks whether all the given simple nano-patterns are used in this method.
     *
     * @param patterns The patterns to match.
     * @return true if all the given simple nano-patterns are used in this method, otherwise false.
     */
    public boolean hasSimpleNanoPatterns(String... patterns) {
        List<String> patternList = findSimpleNanoPatterns();
        for (String pattern : patterns) {
            if (!patternList.contains(pattern)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a list of advanced nano-patterns that are used within this method.
     *
     * @param cached Whether to used the cached list from prior lookups or not.
     * @return A list of advanced nano-patterns that are used within this method.
     */
    public List<String> findAdvancedNanoPatterns(boolean cached) {
        if (cached && advancedNanoPatterns != null) {
            return advancedNanoPatterns;
        }
        List<String> matching = new ArrayList<>();
        AbstractInsnNode[] instructions = instructions().toArray();
        for (AbstractInsnNode insn : instructions) {
            for (AdvancedNanoPattern pattern : ADVANCED_NANO_PATTERNS) {
                if (pattern.matches(insn)) {
                    matching.add(pattern.info().name());
                }
            }
        }
        return (advancedNanoPatterns = matching);
    }

    /**
     * Gets a list of simple nano-patterns that are used within this method.
     *
     * @return A list of simple nano-patterns that are used within this method.
     */
    public List<String> findAdvancedNanoPatterns() {
        return findAdvancedNanoPatterns(true);
    }

    /**
     * Checks whether all the given advanced nano-patterns are used in this method.
     *
     * @param patterns The patterns to match.
     * @return true if all the given advanced nano-patterns are used in this method, otherwise false.
     */
    public boolean hasAdvancedNanoPatterns(String... patterns) {
        List<String> patternList = findAdvancedNanoPatterns();
        for (String pattern : patterns) {
            if (!patternList.contains(pattern)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the types of this method's parameters.
     *
     * @return The types of this method's parameters.
     */
    public Type[] parameterTypes() {
        if (types == null) {
            types = Type.getArgumentTypes(desc());
        }
        return types;
    }

    /**
     * Gets the amount of parameters in this method's desc.
     *
     * @return The amount of parameters in this method's desc.
     */
    public int parameters() {
        if (types == null) {
            types = Type.getArgumentTypes(desc());
        }
        return types.length;
    }

    /**
     * Fetches the descriptor of the parameter at the given index.
     *
     * @param index The index of the parameter to get.
     * @return The descriptor of the parameter at the given index.
     */
    public String parameterAt(int index) {
        if (types == null) {
            types = Type.getArgumentTypes(desc());
        }
        if (index < 0 || index > types.length) {
            throw new IndexOutOfBoundsException();
        }
        return types[index].getDescriptor();
    }

    /**
     * Creates a ControlFlowGraph for this method.
     *
     * @param cached Retrieve by cache, if the graph has been built before.
     * @return A ControlFlowGraph for this method.
     */
    public Optional<ControlFlowGraph> cfg(boolean cached) {
        if (!cached || cfg == null) {
            cfg = ControlFlowGraph.create(this);
        }
        return Optional.ofNullable(cfg);
    }

    /**
     * Creates an ExprTree for this method.
     * <p>
     * This should not be used if speed is an issue, but used in parallel building.
     *
     * @param cached Retrieve by cache, if the tree has been built before.
     * @return An ExprTree for this method.
     */
    public Optional<ExprTree> tree(boolean cached) {
        if (!cached || tree == null) {
            Optional<ExprTree> opt = ExprTreeBuilder.build(this);
            if (cached && opt.isPresent()) {
                tree = opt.get();
            }
            return opt;
        }
        return Optional.of(tree);
    }

    /**
     * Creates an ExprTree for this method.
     * <p>
     * This should not be used if speed is an issue, but used in parallel building.
     *
     * @return An ExprTree for this method.
     */
    public Optional<ExprTree> tree() {
        return tree(false);
    }

    /**
     * Creates a ControlFlowGraph for this method.
     *
     * @return A ControlFlowGraph for this method.
     */
    public Optional<ControlFlowGraph> cfg() {
        return cfg(true);
    }

    /**
     * Clears the cache of key to ClassMethod
     */
    public static void clearKeyCache() {
        CACHED.clear();
        CACHED = new ConcurrentHashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ClassMethod && ((ClassMethod) o).method.equals(method)) ||
                (o instanceof MethodNode && method.equals(o));
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}