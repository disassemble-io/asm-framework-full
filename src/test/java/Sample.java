/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
public class Sample {

    private int var;

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
}
