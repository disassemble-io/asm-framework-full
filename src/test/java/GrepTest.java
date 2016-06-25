import io.disassemble.asm.util.Grep;
import org.junit.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tyler Sedlar
 * @since 6/24/16
 */
public class GrepTest {

    @Test
    public void test() {
        String test = "client.qj(765, 503, 116)";
        Pattern pattern = Pattern.compile("(.+)\\.(.+)\\(765, 503, (.+)\\)");
        long start = System.nanoTime();
        Matcher matcher = pattern.matcher(test);
        boolean found = matcher.find();
        long end = System.nanoTime();
        System.out.println("regex: " + (end - start) + "ns");
        System.out.printf("       %.5f millis\n", (end - start) / 1e6);
        System.out.println("       " + matcher.group(3));
        Grep grep = new Grep("{class}.{method}(765, 503, {rev})");
        start = System.nanoTime();
        Map<String, String> matches = grep.exec(test);
        end = System.nanoTime();
        System.out.println("t-regex: " + (end - start) + "ns");
        System.out.printf("         %.5f millis\n", (end - start) / 1e6);
        System.out.println("         " + matches.get("class"));
        System.out.println("         " + matches.get("method"));
        System.out.println("         " + matches.get("rev"));
    }
}
