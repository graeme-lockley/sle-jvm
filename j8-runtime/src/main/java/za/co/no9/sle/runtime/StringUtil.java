package za.co.no9.sle.runtime;

import java.util.function.Function;

public class StringUtil {
    public static final Object length =
            (Function<Object, Object>) s -> ((String) s).length();
}
