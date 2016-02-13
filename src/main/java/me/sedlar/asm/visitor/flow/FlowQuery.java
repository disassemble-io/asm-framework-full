package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.util.StringMatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class FlowQuery implements Opcodes {

    public static final int DEFAULT_MAX_DISTANCE = 10;

    private final List<Predicate<ExecutionNode>> predicates = new ArrayList<>();
    private final List<Integer> branches = new ArrayList<>();
    private final Map<Integer, Integer> dists = new HashMap<>();
    private final Map<Integer, String> names = new HashMap<>();

    public List<Predicate<ExecutionNode>> predicates() {
        return predicates;
    }

    /**
     * Chains the given predicate as a query.
     *
     * @param predicate The predicate to chain.
     * @return This FlowQuery chained with the given predicate.
     */
    public FlowQuery query(Predicate<ExecutionNode> predicate) {
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
        predicates.add(insn -> (Arrays.binarySearch(opcodes, insn.source.instruction.getOpcode()) >= 0));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an if statement.
     *
     * @return This FlowQuery chained with a predicate checking for an if statement.
     */
    public FlowQuery stmtIf() {
        predicates.add(insn -> (insn.source.instruction.getOpcode() >= IFEQ &&
                insn.source.instruction.getOpcode() <= IF_ACMPNE) ||
                (insn.source.instruction.getOpcode() >= IFNULL && insn.source.instruction.getOpcode() <= IFNONNULL));
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
        predicates.add(insn -> (insn.source.instruction.getOpcode() >= ISTORE &&
                insn.source.instruction.getOpcode() <= SASTORE) &&
                (var == null || var.test(((VarInsnNode) insn.source.instruction).var)));
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
        predicates.add(insn -> (insn.source.instruction.getOpcode() >= ILOAD &&
                insn.source.instruction.getOpcode() <= SALOAD) &&
                (var == null || var.test(((VarInsnNode) insn.source.instruction).var)));
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
        predicates.add(insn -> (insn.source.instruction instanceof TypeInsnNode &&
                (type == null || StringMatcher.matches(type.get(), ((TypeInsnNode) insn.source.instruction).desc))));
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

    private Predicate<ExecutionNode> fieldPredicate(int opcode, Supplier<String> owner, Supplier<String> desc) {
        return insn -> {
            if (insn.source.instruction.getOpcode() == opcode) {
                FieldInsnNode fin = (FieldInsnNode) insn.source.instruction;
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

    private Predicate<ExecutionNode> methodPredicate(int opcode, Supplier<String> owner, Supplier<String> desc) {
        return insn -> {
            if (insn.source.instruction.getOpcode() == opcode) {
                MethodInsnNode min = (MethodInsnNode) insn.source.instruction;
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
        predicates.add(fieldPredicate(INVOKEVIRTUAL, owner, desc));
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
        predicates.add(fieldPredicate(INVOKESTATIC, owner, desc));
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
        predicates.add(fieldPredicate(INVOKESPECIAL, owner, desc));
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
        predicates.add(fieldPredicate(INVOKEINTERFACE, owner, desc));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an IntInsnNode matching the given predicate.
     *
     * @param operand The predicate to match the operand against.
     * @return This FlowQuery chained with a predicate checking for an IntInsnNode matching the given predicate.
     */
    public FlowQuery stmtPush(Predicate<Integer> operand) {
        predicates.add(insn -> (insn.source.instruction.getOpcode() == BIPUSH ||
                insn.source.instruction.getOpcode() == SIPUSH) &&
                (operand == null || operand.test(((IntInsnNode) insn.source.instruction).operand)));
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
        predicates.add(insn -> (insn.source.instruction instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get().equals(((LdcInsnNode) insn.source.instruction).cst))));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The int constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtIntConstant(Supplier<Integer> constant) {
        predicates.add(insn -> (insn.source.instruction instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.source.instruction).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The long constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtLongConstant(Supplier<Long> constant) {
        predicates.add(insn -> (insn.source.instruction instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.source.instruction).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The double constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtDoubleConstant(Supplier<Double> constant) {
        predicates.add(insn -> (insn.source.instruction instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.source.instruction).cst)));
        return this;
    }

    /**
     * Chains this FlowQuery with a predicate checking for an LdcInsnNode matching the given predicate.
     *
     * @param constant The short constant to check.
     * @return This FlowQuery chained with a predicate checking for an LdcInsnNode matching the given predicate.
     */
    public FlowQuery stmtShortConstant(Supplier<Short> constant) {
        predicates.add(insn -> (insn.source.instruction instanceof LdcInsnNode &&
                (constant == null || constant.get() == null ||
                        constant.get() == ((LdcInsnNode) insn.source.instruction).cst)));
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
     * Checks whether there is a branch at the given index.
     *
     * @param index The index to check for a branch at.
     * @return <t>true</t> if there is a branch at the given index, otherwise <t>false</t>.
     */
    public boolean branchesAt(int index) {
        return branches.contains(index);
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
}
