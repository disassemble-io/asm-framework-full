/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
public class Sample {

    private int var;

    private static int v1 = 10, v2 = 20;
    private static int[] v3;

    private int v4 = 30;

    public void setter(int var) {
        this.var = var;
    }

    public Sample chainedSetter(int var) {
        setter(var);
        return this;
    }

    public void cfg(int x, int y) {
        int s = 0, d = 0;
        while (x < y) {
            x += 3;
            y += 2;
            if (x + y < 100) {
                s += (x + y);
            } else {
                d += (x + y);
            }
        }
    }

    void t(int var1) {
        /**
         * Any stack segment should be iterated in reverse.
         * - If a unary/binary expr is hit, it should start a new tree.
         *  - POP 'X' amount of previous instructions to the new tree.
         *  - If the expr right before is a unary/binary expr, it should start a new inner tree.
         *   - This should let the parent tree POP again. As if it never originally did.
         *  - any other instructions should be added to the 'root.'
         * - After a full iteration, the root trees should be sorted in reverse.
         ===================================================
             new-insn: LDC java.lang.Integer -33096101 <-- peekx2 == LabelNode
             new-insn: GETSTATIC Sample.v1 I
             binary-insn: IMUL
             new-insn: GETSTATIC Sample.v3 [I
             new-insn: GETSTATIC Sample.v2 I
             new-insn: LDC java.lang.Integer 1521728277
             binary-insn: IMUL
             binary-insn: IALOAD
             binary-insn: IF_ICMPLE
             new-insn: GETSTATIC Sample.v1 I
             new-insn: GETSTATIC Sample.v3 [I
             new-insn: LDC java.lang.Integer 1521728277
             new-insn: GETSTATIC Sample.v2 I
             binary-insn: IMUL
             binary-insn: IALOAD
             new-insn: LDC java.lang.Integer -1944452653
             binary-insn: IMUL
             binary-insn: ISUB
             unary-insn: PUTSTATIC Sample.v1 I <-- POPx1 == LabelNode (end <- loop)
         ===================================================
            WHILE_LOOP
                IF_ICMPLE
                    IALOAD (POP 2 -- BinaryOperation)
                        IMUL (POP 2 -- BinaryOperation)
                            LDC java.lang.Integer 1521728277
                            GETSTATIC Sample.v2 I
                        GETSTATIC Sample.v3 [I
                    IMUL (POP 2 - BinaryOpertion)
                        GETSTATIC Sample.v1 I
                        LDC java.lang.Integer -33096101
                PUTSTATIC Sample.v1 I (Assumed POP 1)
                    ISUB (POP 2 - BinaryOperation)
                        IMUL (POP 2 -- BinaryOperation)
                            LDC java.lang.Integer -1944452653
                            IALOAD (POP 2 -- BinaryOperation)
                                IMUL (POP 2 -- BinaryOperation)
                                    GETSTATIC Sample.v2 I
                                    LDC java.lang.Integer 1521728277
                                GETSTATIC Sample.v3 [I
                        GETSTATIC Sample.v1 I
         */
        while(-33096101 * v1 > v3[v2 * 1521728277]) {
            v1 -= v3[1521728277 * v2] * -1944452653;
        }
    }

    int u(int p1) {
        for (int i = 0; i < p1; i++) {
            p1 -= i;
        }
        return p1;
    }

    void euclid() {
        v1 = u(2055148518) * -1620382429; // encoder
        v3 = new int[v1 * 908634763]; // decoder
    }

    int call(int a, int b) {
        System.out.println(a + ", " + b);
        return (a + b);
    }
}
