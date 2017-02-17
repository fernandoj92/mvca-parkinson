package voltric.util.counter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For generating indices and default getName. Each time the method {@code next} is
 * called, an index and a getName are generated with the current count incremented.
 * <p>
 * The {@code encounterName} should be called when a getName is encountered, so
 * that it checks to see if the given getName clashes with the names that might be
 * generated.
 *
 * @author leonard
 *
 */
public class Counter {
    
    private int current = 0;
    private final String prefix;
    private final Pattern pattern;

    public Counter(String prefix) {
        this.prefix = prefix;
        this.pattern = Pattern.compile(prefix + "(\\d*)");
    }

    public synchronized CounterInstance next() {
        CounterInstance instance = new CounterInstance(current, createName());
        current++;
        return instance;
    }

    public synchronized int nextIndex() {
        return current++;
    }

    public synchronized void encounterName(String name) {
        Matcher match = pattern.matcher(name);
        if (match.matches()) {
            int number = Integer.parseInt(match.group(1));
            if (number >= current)
                current = number + 1;
        }
    }

    private String createName() {
        return prefix + current;
    }
}
