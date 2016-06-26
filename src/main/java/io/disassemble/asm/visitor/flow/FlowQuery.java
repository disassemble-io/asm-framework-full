package io.disassemble.asm.visitor.flow;

import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Query;
import io.disassemble.asm.util.StringMatcher;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class FlowQuery extends Query<FlowQueryResult, ControlFlowGraph> {
    public static final int DEFAULT_MAX_DISTANCE = 10;
    private final List<Predicate<BasicInstruction>> predicates = new ArrayList<>();
    private final List<Integer> branches = new ArrayList<>();
    private final Map<Integer, Integer> dists = new HashMap<>();
    private final Map<Integer, String> names = new HashMap<>();
    private final Map<Integer, BranchType> branchTypes = new HashMap<>();
    private final List<Integer> loops = new ArrayList<>();
    private final List<Integer> restrictedLoops = new ArrayList<>();
    private boolean stopAtFirst = true;
    private Predicate<ClassFactory> restrictToClass;
    private Predicate<ClassMethod> restrictToMethod;

    @Override
    public Optional<List<FlowQueryResult>> find(ControlFlowGraph cfg) {
        if (restrictToClass == null || restrictToClass.test(cfg.method.owner)) {
            if (restrictToMethod == null || restrictToMethod.test(cfg.method)) {
                List<FlowQueryResult> results = cfg.execution().query(this);
                if (stopAtFirst && !results.isEmpty()) {
                    lock();
                }
                return Optional.ofNullable(results);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets a list of predicates constructed from this query.
     *
     * @return A list of predicates constructed from this query.
     */
    public List<Predicate<BasicInstruction>> predicates() {
        return predicates;
    }

    /**
     * Sets this FlowQuery to continuously fetch all results, not only the first.
     *
     * @return This FlowQuery chained to continuously fetch all results, not only the first.
     */
    public FlowQuery continuous() {
        stopAtFirst = false;
        return this;
    }

    /**
     * Sets this FlowQuery to only query classes that match the given predicate.
     *
     * @param predicate The predicate to match against.
     * @return This FlowQuery chained to only query classes that match the given predicate.
     */
    public FlowQuery restrictToClass(Predicate<ClassFactory> predicate) {
        restrictToClass = predicate;
        return this;
    }

    /**
     * Sets this FlowQuery to only query methods that match the given predicate.
     *
     * @param predicate The predicate to match against.
     * @return This FlowQuery chained to only query methods that match the given predicate.
     */
    public FlowQuery restrictToMethod(Predicate<ClassMethod> predicate) {
        restrictToMethod = predicate;
        return this;
    }

    /**
     * Sets this FlowQuery to only query methods that match the given desc.
     *
     * @param desc The desc to match against.
     * @return This FlowQuery chained to only query methods that match the given desc.
     */
    public FlowQuery restrictToMethodDesc(Supplier<String> desc) {
        return restrictToMethod(cm -> StringMatcher.matches(desc.get(), cm.desc()));
    }

    /**
     * Chains the given predicate as a query.
     *
     * @param predicate The predicate to chain.
     * @return This FlowQuery chained with the given predicate.
     */
    public FlowQuery query(Predicate<BasicInstruction> predicate) {
        predicates.add(predicate);
        return this;
    }

    /**
     * Chains the given opcodes as an opcode predicate.
     *
     * @param opcodes The possible opcodes to query for.
     * @return This FlowQuery chained with the given opcodes.
     */
    public FlowQuery opcode(int... opcodes) {
        Arrays.sort(opcodes);
        predicates.add(insn -> (Arrays.binarySearch(opcodes, insn.insn.getOpcode()) >= 0));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an if statement.
     *
     * @return This FlowQuery chained with a predicate checking for an if statement.
     */
    public FlowQuery stmtIf() {
        predicates.add(insn -> (insn.insn.getOpcode() >= IFEQ &&
                insn.insn.getOpcode() <= IF_ACMPNE) ||
                (insn.insn.getOpcode() >= IFNULL && insn.insn.getOpcode() <= IFNONNULL));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a store statement,
     * consisting of:
     * <pre>
     * ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
     * </pre>
     *
     * @param var The predicate to match the value against.
     * @return This FlowQuery chained with a predicate checking for a store statement.
     */
    public FlowQuery stmtStore(Predicate<Integer> var) {
        predicates.add(insn -> {
            int op = insn.insn.getOpcode();
            if (op  >= ISTORE && op <= SASTORE) {
                boolean hasVar = (op >= ISTORE && op <= ASTORE);
                return var == null || (hasVar && var.test(((VarInsnNode) insn.insn).var));
            }
            return false;
        });
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a store statement,
     * consisting of:
     * <pre>
     * ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
     * </pre>
     *
     * @return This FlowQuery chained with a predicate checking for a store statement.
     */
    public FlowQuery stmtStore() {
        return stmtStore(null);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a load statement,
     * consisting of:
     * <pre>
     * ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD
     * </pre>
     *
     * @param var The predicate to match the value against.
     * @return This FlowQuery chained with a predicate checking for a load statement.
     */
    public FlowQuery stmtLoad(Predicate<Integer> var) {
        predicates.add(insn -> {
            int op = insn.insn.getOpcode();
            if (op  >= ILOAD && op <= SALOAD) {
                boolean hasVar = (op >= ILOAD && op <= ALOAD);
                return var == null || (hasVar && var.test(((VarInsnNode) insn.insn).var));
            }
            return false;
        });
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a load statement,
     * consisting of:
     * <pre>
     * ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD
     * </pre>
     *
     * @return This FlowQuery chained with a predicate checking for a load statement.
     */
    public FlowQuery stmtLoad() {
        return stmtLoad(null);
    }

    /**
     * Chains this FlowQuery with a predicate checking for an IincInsnNode.
     *
     * @return This FlowQuery chained with a predicate checking for an IincInsnNode.
     */
    public FlowQuery stmtIncrement() {
        return opcode(IINC);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a TypeInsnNode matching the given type.
     *
     * @param type The type to match.
     * @return This FlowQuery chained with a predicate checking for a TypeInsnNode matching the given type.
     */
    public FlowQuery stmtType(Supplier<String> type) {
        predicates.add(insn -> (insn.insn instanceof TypeInsnNode &&
                (type == null || StringMatcher.matches(type.get(), ((TypeInsnNode) insn.insn).desc))));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a TypeInsnNode matching the given type.
     *
     * @return This FlowQuery chained with a predicate checking for a TypeInsnNode matching the given type.
     */
    public FlowQuery stmtType() {
        return stmtType(null);
    }

    private Predicate<BasicInstruction> fieldPredicate(int opcode, Supplier<String> owner, Supplier<String> desc) {
        return insn -> {
            if (insn.insn.getOpcode() == opcode) {
                FieldInsnNode fin = (FieldInsnNode) insn.insn;
                return ((owner == null || owner.get() == null || StringMatcher.matches(owner.get(), fin.owner)) &&
                        (desc == null || desc.get() == null || StringMatcher.matches(desc.get(), fin.desc)));
            }
            return false;
        };
    }

    /**
     * Chains this FlowQuery with a predicate checking for a GETFIELD matching the given arguments.
     *
     * @param owner The field owner to match.
     * @param desc  The field desc to match.
     * @return This FlowQuery chained with a predicate checking for a GETFIELD matching the given arguments.
     */
    public FlowQuery stmtGetField(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(fieldPredicate(GETFIELD, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a GETSTATIC matching the given arguments.
     *
     * @param owner The field owner to match.
     * @param desc  The field desc to match.
     * @return This FlowQuery chained with a predicate checking for a GETSTATIC matching the given arguments.
     */
    public FlowQuery stmtGetStatic(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(fieldPredicate(GETSTATIC, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a PUTFIELD matching the given arguments.
     *
     * @param owner The field owner to match.
     * @param desc  The field desc to match.
     * @return This FlowQuery chained with a predicate checking for a PUTFIELD matching the given arguments.
     */
    public FlowQuery stmtPutField(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(fieldPredicate(PUTFIELD, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for a PUTSTATIC matching the given arguments.
     *
     * @param owner The field owner to match.
     * @param desc  The field desc to match.
     * @return This FlowQuery chained with a predicate checking for a PUTSTATIC matching the given arguments.
     */
    public FlowQuery stmtPutStatic(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(fieldPredicate(PUTSTATIC, owner, desc));
        return this;
    }

    private Predicate<BasicInstruction> methodPredicate(int opcode, Supplier<String> owner, Supplier<String> desc) {
        return insn -> {
            if (insn.insn.getOpcode() == opcode) {
                MethodInsnNode min = (MethodInsnNode) insn.insn;
                return ((owner == null || owner.get() == null || StringMatcher.matches(owner.get(), min.owner)) &&
                        (desc == null || desc.get() == null || StringMatcher.matches(desc.get(), min.desc)));
            }
            return false;
        };
    }

    /**
     * Chains this FlowQuery with a predicate checking for an INVOKEVIRTUAL matching the given arguments.
     *
     * @param owner The method owner to match.
     * @param desc  The method desc to match.
     * @return This FlowQuery chained with a predicate checking for an INVOKEVIRTUAL matching the given arguments.
     */
    public FlowQuery stmtInvokeVirtual(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(methodPredicate(INVOKEVIRTUAL, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an INVOKESTATIC matching the given arguments.
     *
     * @param owner The method owner to match.
     * @param desc  The method desc to match.
     * @return This FlowQuery chained with a predicate checking for an INVOKESTATIC matching the given arguments.
     */
    public FlowQuery stmtInvokeStatic(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(methodPredicate(INVOKESTATIC, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an INVOKESPECIAL matching the given arguments.
     *
     * @param owner The method owner to match.
     * @param desc  The method desc to match.
     * @return This FlowQuery chained with a predicate checking for an INVOKESPECIAL matching the given arguments.
     */
    public FlowQuery stmtInvokeSpecial(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(methodPredicate(INVOKESPECIAL, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an INVOKEINTERFACE matching the given arguments.
     *
     * @param owner The method owner to match.
     * @param desc  The method desc to match.
     * @return This FlowQuery chained with a predicate checking for an INVOKEINTERFACE matching the given arguments.
     */
    public FlowQuery stmtInvokeInterface(Supplier<String> owner, Supplier<String> desc) {
        predicates.add(methodPredicate(INVOKEINTERFACE, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an IntInsnNode matching the given predicate.
     *
     * @param operand The predicate to match the operand against.
     * @return This FlowQuery chained with a predicate checking for an IntInsnNode matching the given predicate.
     */
    public FlowQuery stmtPush(Predicate<Integer> operand) {
        predicates.add(insn -> (insn.insn.getOpcode() == BIPUSH ||
                insn.insn.getOpcode() == SIPUSH) &&
                (operand == null || operand.test(((IntInsnNode) insn.insn).operand)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an IntInsnNode matching the given predicate.
     *
     * @return This FlowQuery chained with a predicate checking for an IntInsnNode matching the given predicate.
     */
    public FlowQuery stmtPush() {
        return stmtPush(null);
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The string constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtStringConstant(Supplier<String> constant) {
        predicates.add(insn -> (insn.insn instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get().equals(((LdcInsnNode) insn.insn).cst))));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The int constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtIntConstant(Supplier<Integer> constant) {
        predicates.add(insn -> (insn.insn instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.insn).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The long constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtLongConstant(Supplier<Long> constant) {
        predicates.add(insn -> (insn.insn instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.insn).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The double constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtDoubleConstant(Supplier<Double> constant) {
        predicates.add(insn -> (insn.insn instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.insn).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The short constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtShortConstant(Supplier<Short> constant) {
        predicates.add(insn -> (insn.insn instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.insn).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode.
     *
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode.
     */
    public FlowQuery stmtConstant() {
        return opcode(LDC);
    }

    /**
     * Chains this FlowQuery with a predicate checking for an adding instruction.
     *
     * @return This FlowQuery chained with a predicate checking for an adding instruction.
     */
    public FlowQuery stmtAdd() {
        return opcode(IADD, LADD, FADD, DADD);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a subtracting instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a subtracting instruction.
     */
    public FlowQuery stmtSubtract() {
        return opcode(ISUB, LSUB, FSUB, DSUB);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a multiplying instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a multiplying instruction.
     */
    public FlowQuery stmtMultiply() {
        return opcode(IMUL, LMUL, FMUL, DMUL);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a dividing instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a dividing instruction.
     */
    public FlowQuery stmtDivide() {
        return opcode(IDIV, LDIV, FDIV, DDIV);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a remainder instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a remainder instruction.
     */
    public FlowQuery stmtRemainder() {
        return opcode(IREM, LREM, FREM, DREM);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a negate instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a negate instruction.
     */
    public FlowQuery stmtNegate() {
        return opcode(INEG, LNEG, FNEG, DNEG);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a left-shift instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a left-shift instruction.
     */
    public FlowQuery stmtLeftShift() {
        return opcode(ISHL, LSHL);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a right-shift instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a right-shift instruction.
     */
    public FlowQuery stmtRightShift() {
        return opcode(ISHR, LSHR);
    }

    /**
     * Chains this FlowQuery with a predicate checking for an unsigned right-shift instruction.
     *
     * @return This FlowQuery chained with a predicate checking for an unsigned right-shift instruction.
     */
    public FlowQuery stmtUnsignedRightShift() {
        return opcode(IUSHR, LUSHR);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a bitwise and instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a bitwise and instruction.
     */
    public FlowQuery stmtBitwiseAnd() {
        return opcode(IAND, LAND);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a bitwise or instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a bitwise or instruction.
     */
    public FlowQuery stmtBitwiseOr() {
        return opcode(IOR, LOR);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a bitwise xor instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a bitwise xor instruction.
     */
    public FlowQuery stmtBitwiseXor() {
        return opcode(IXOR, LXOR);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-int instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-int instruction.
     */
    public FlowQuery stmtCastToInt() {
        return opcode(L2I, F2I, D2I);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-long instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-long instruction.
     */
    public FlowQuery stmtCastToLong() {
        return opcode(I2L, F2L, D2L);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-float instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-float instruction.
     */
    public FlowQuery stmtCastToFloat() {
        return opcode(I2F, L2F, D2F);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-double instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-double instruction.
     */
    public FlowQuery stmtCastToDouble() {
        return opcode(I2D, L2D, F2D);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-byte instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-byte instruction.
     */
    public FlowQuery stmtCastToByte() {
        return opcode(I2B);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-char instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-char instruction.
     */
    public FlowQuery stmtCastToChar() {
        return opcode(I2C);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a cast-to-short instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a cast-to-short instruction.
     */
    public FlowQuery stmtCastToShort() {
        return opcode(I2S);
    }

    /**
     * Chains this FlowQuery with a predicate checking for a return instruction.
     *
     * @return This FlowQuery chained with a predicate checking for a return instruction.
     */
    public FlowQuery stmtReturn() {
        return opcode(IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN);
    }

    /**
     * Branches at the prior chained predicate.
     *
     * @return This FlowQuery with a branch at the prior predicate.
     */
    public FlowQuery branch() {
        branches.add(predicates.size() - 1);
        return this;
    }

    /**
     * Branches at the prior chained predicate, accepting only a true branch.
     *
     * @return This FlowQuery with a branch at the prior predicate, accepting only a true branch.
     */
    public FlowQuery branchTrue() {
        branch();
        branchTypes.put(predicates.size() - 1, BranchType.TRUE);
        return this;
    }

    /**
     * Branches at the prior chained predicate, accepting only a false branch.
     *
     * @return This FlowQuery with a branch at the prior predicate, accepting only a false branch.
     */
    public FlowQuery branchFalse() {
        branch();
        branchTypes.put(predicates.size() - 1, BranchType.FALSE);
        return this;
    }

    /**
     * Checks whether there is a branch at the given index.
     *
     * @param index The index to check for a branch at.
     * @return true if there is a branch at the given index, otherwise false.
     */
    public boolean branchesAt(int index) {
        return branches.contains(index);
    }

    /**
     * Gets the BranchType for the given index.
     *
     * @param index The index to check at.
     * @return The BranchType for the given index.
     */
    public BranchType branchTypeAt(int index) {
        return (branchTypes.containsKey(index) ? branchTypes.get(index) : BranchType.DEFAULT);
    }

    /**
     * Sets the search distance for the prior predicate.
     *
     * @param maxDist The maximum distance to search.
     * @return This FlowQuery with a distance constraint at the prior predicate.
     */
    public FlowQuery dist(int maxDist) {
        dists.put(predicates.size() - 1, maxDist);
        return this;
    }

    /**
     * Gets the max search distance for the predicate at the given index.
     *
     * @param index The index to check at.
     * @return The max search distance for the predicate at the given index.
     */
    public int distAt(int index) {
        return (dists.containsKey(index) ? dists.get(index) : DEFAULT_MAX_DISTANCE);
    }

    /**
     * Gives the prior predicate the given name.
     *
     * @param name The name to give the prior predicate.
     * @return This FlowQuery with a name at the prior predicate.
     */
    public FlowQuery name(String name) {
        names.put(predicates.size() - 1, name);
        return this;
    }

    /**
     * Gets the name for the predicate at the given index.
     *
     * @param index The index to check at.
     * @return The name for the predicate at the given index.
     */
    public String nameAt(int index) {
        return names.get(index);
    }

    /**
     * Loops at the prior predicate.
     *
     * @return This FlowQuery chained with a loop query at the prior predicate.
     */
    public FlowQuery loops() {
        loops.add(predicates.size() - 1);
        return this;
    }

    /**
     * Does not loop at the prior predicate.
     *
     * @return This FlowQuery chained with a restricted-loop query at the prior predicate.
     */
    public FlowQuery doesNotLoop() {
        restrictedLoops.add(predicates.size() - 1);
        return this;
    }

    /**
     * Checks whether there has been a loop query at the given index.
     *
     * @param index The index to check at.
     * @return true if there is a loop query at the given index, otherwise false.
     */
    public boolean loopsAt(int index) {
        return loops.contains(index);
    }

    /**
     * Checks whether there has been a restricted-loop query at the given index.
     *
     * @param index The index to check at.
     * @return true if there is a restricted-loop query at the given index, otherwise false.
     */
    public boolean doesNotLoopAt(int index) {
        return restrictedLoops.contains(index);
    }

    public enum BranchType {
        DEFAULT, TRUE, FALSE
    }
}