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
}
