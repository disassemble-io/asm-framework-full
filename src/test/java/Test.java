import java.math.BigInteger;

/**
 * @author Tyler Sedlar
 */
public class Test {

    private class BigNumber extends BigInteger {

        public BigNumber(String val) {
            super(val);
        }
    }

    @org.junit.Test
    @SuppressWarnings("all")
    public void testEuclid() {
        int bits = 64;
        BigInteger quotient = new BigInteger("-2067339215");
        quotient = new BigInteger("-8226704349782346459");
        try {
            BigInteger shift = BigInteger.ONE.shiftLeft(bits);
            BigInteger modulo = quotient.modInverse(shift);
            System.out.println("long-modulo: " + modulo.longValue());
            System.out.println("int-modulo: " + modulo.intValue());
            assert modulo.longValue() * quotient.longValue() == 1L;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
    }
}