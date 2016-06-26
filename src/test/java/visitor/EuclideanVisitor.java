package visitor;

import io.disassemble.asm.visitor.expr.ExprTreeVisitor;
import io.disassemble.asm.visitor.expr.node.BasicExpr;
import io.disassemble.asm.visitor.expr.node.ConstExpr;
import io.disassemble.asm.visitor.expr.node.FieldExpr;
import io.disassemble.asm.visitor.expr.node.MathExpr;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/18/16
 */
public class EuclideanVisitor extends ExprTreeVisitor {

    private static final BigInteger X32_ENCODER = BigInteger.ONE.shiftLeft(32);

    /**
     * This constant is used for modInverse, and should be favored over X32_ENCODER.
     *  - The only difference is calling #intValue or #longValue on its result.
     */
    private static final BigInteger X64_ENCODER = BigInteger.ONE.shiftLeft(64);

    private final ConcurrentMap<String, List<Number>> decoders, encoders, excessEncoders;
    private final ConcurrentMap<Number, Double> weights;

    public EuclideanVisitor() {
        this.decoders = new ConcurrentHashMap<>();
        this.encoders = new ConcurrentHashMap<>();
        this.excessEncoders = new ConcurrentHashMap<>();
        this.weights = new ConcurrentHashMap<>();
    }

    public Map<String, List<Number>> decoders() {
        return decoders;
    }

    public Map<String, List<Number>> encoders() {
        return encoders;
    }

    @Override
    public void visitMathExpr(MathExpr expr) {
        // Ensure that parent is not PUTFIELD/PUTSTATIC (encoder)
        if (expr.parent() != null && expr.parent() instanceof FieldExpr && ((FieldExpr) expr.parent()).putter()) {
            return;
        }
        //  IMUL | LMUL
        //      FieldExpr
        //      ConstExpr
        if (expr.opcode() == IMUL || expr.opcode() == LMUL) {
            expr.field().ifPresent(field -> {
                if (field.getter()) {
                    expr.constant().ifPresent(constant -> handle(field, constant, decoders));
                }
            });
        }
    }

    @Override
    public void visitConstExpr(ConstExpr expr) {
        BasicExpr parent = expr.parent();
        //  FieldExpr
        //      ......
        //          IMUL | LMUL
        //              ConstExpr
        if (parent != null && (parent.opcode() == IMUL || parent.opcode() == LMUL)) {
            // Walk up the tree for the field this multiplier is acting upon.
            //  - It is possible for an ADD/SUB insn to be placed beforehand.
            while ((parent = parent.parent()) != null) {
                if (parent instanceof FieldExpr) {
                    break;
                }
            }
            if (parent != null) {
                FieldExpr field = (FieldExpr) parent;
                if (field.putter()) { // this could be asserted.
                    handle(field, expr, encoders);
                }
            }
        }
    }

    private void handle(FieldExpr field, ConstExpr expr, Map<String, List<Number>> map) {
        Number mult = expr.number();
        boolean isLong = (mult instanceof Long);
        boolean isInt = (mult instanceof Integer);
        // multiplicative inverses cannot be even, let's filter them out.
        if ((isLong && ((long) mult % 2L) == 0L) || (isInt && ((int) mult % 2) == 0)) {
            return;
        }
        String key = field.key();
        double weight = 1.0D;
        if (field.getter()) {
            BasicExpr l = null, l2 = null, r = null;
            if (expr.parent().parent() != null) {
                if ((l = expr.parent().parent().left()) != null) {
                    l2 = l.left();
                }
                r = expr.parent().parent().right();
            }
            // The following are not 'stable,' but do have some valid multipliers.
            // They're weighted lower than a standard pattern mult.
            if (l2 != null && (l2.opcode() == GOTO || l2.insn() instanceof LabelNode)) {
                weight = 0.25D;
            } else if (r != null && (r.opcode() == GOTO || r.insn() instanceof LabelNode)) {
                weight = 0.25D;
            } else if (l2 == null && l == null && r == null) {
                weight = 0.1D;
            }
        } else if (field.right() != null) {
            BasicExpr l = field.left(), r = field.right();
            // The following begin new frames and should not be considered for encoders.
            if (r.opcode() == GOTO) {
                if (!excessEncoders.containsKey(key)) {
                    excessEncoders.put(key, new ArrayList<>());
                }
                excessEncoders.get(key).add(expr.number());
                return;
            } else if (l != null && l.insn() instanceof LabelNode && r.insn() instanceof LabelNode &&
                    r.right() != null && r.right().insn() instanceof LineNumberNode) {
                return;
            }
        }
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        List<Number> mults = map.get(key);
        if (!mults.contains(mult)) {
            mults.add(mult);
        }
        if (!weights.containsKey(mult)) {
            weights.put(mult, 0D);
        }
        weights.put(mult, weights.get(mult) + weight);
    }

    private Number inverseOf(Number encoder) {
        BigInteger quotient = new BigInteger(encoder.toString());
        BigInteger mod = quotient.modInverse(X64_ENCODER);
        boolean isLong = (encoder instanceof Long);
        // this does not need to be checked since we only match longs/ints.
        return (isLong ? mod.longValue() : mod.intValue());
    }

    public Map<String, Number> match() {
        Map<String, Number> matches = new HashMap<>();
        decoders.keySet().forEach(decKey -> {
            List<Number> decs = decoders.get(decKey);
            if (decs.size() == 1) {
                // let's assume this is completely valid, since it's the only decoder.
                matches.put(decKey, decs.get(0));
            } else {
                encoders.forEach((encKey, encs) -> {
                    if (encs.size() == 1) {
                        // let's assume the inverse is completely valid, since it's the only encoder.
                        Number encoder = encs.get(0);
                        // since it's an encoder, let's get the multiplicative inverse of it (decoder).
                        Number decoder = inverseOf(encoder);
                        // let's only put it in if it hasn't been matched
                        if (!matches.containsKey(encKey)) {
                            matches.put(encKey, decoder);
                        }
                    }
                    for (Number dec : decs) {
                        encs.stream().filter(enc -> {
                            // let's filter and check that decoder * encoder == 1,
                            // since multiplicative identity of multiplicative inverses states as such.
                            if (dec instanceof Long && enc instanceof Long) {
                                return (dec.longValue() * enc.longValue()) == 1L;
                            } else if (dec instanceof Integer && enc instanceof Integer) {
                                return (dec.intValue() * enc.intValue()) == 1;
                            }
                            return false;
                        }).forEach(enc -> {
                            String[] keys = {decKey, encKey};
                            for (String key : keys) {
                                matches.put(key, dec);
                            }
                        });
                    }
                });
            }
        });
        // loop through and find unmatched decoders without encoders
        decoders.keySet().forEach(key -> {
            if (!encoders.containsKey(key) && !matches.containsKey(key)) {
                List<Number> decs = decoders.get(key);
                if (decs.size() > 1) {
                    // let's use the decoder that has the highest weight.
                    decs.sort((a, b) -> Double.compare(weights.get(b), weights.get(a)));
                    matches.put(key, decs.get(0));
                }
            }
        });
        // loop through and find unmatched encoders without decoders
        encoders.keySet().forEach(key -> {
            if (!decoders.containsKey(key) && !matches.containsKey(key)) {
                List<Number> encs = encoders.get(key);
                if (encs.size() > 1) {
                    // let's use the encoder that has the highest weight.
                    encs.sort((a, b) -> Double.compare(weights.get(b), weights.get(a)));
                    matches.put(key, inverseOf(encs.get(0)));
                }
            }
        });
        // loop through excess encoders and find near matches.
        excessEncoders.keySet().forEach(key -> {
            if (!decoders.containsKey(key) && !encoders.containsKey(key) && !matches.containsKey(key)) {
                List<Number> encs = excessEncoders.get(key);
                Set<Number> strippedEncs = new HashSet<>(encs);
                if (strippedEncs.size() == 1) {
                    matches.put(key, inverseOf(strippedEncs.iterator().next()));
                } else {
                    System.out.println("Too many excess encoders @ " + key);
                }
            }
        });
        return matches;
    }
}