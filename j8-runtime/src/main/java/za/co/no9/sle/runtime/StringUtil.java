package za.co.no9.sle.runtime;

import java.util.function.Function;

public class StringUtil {
    public static final Object length =
            (Function<Object, Object>) s -> ((String) s).length();

    public static final Object reverse =
            (Function<Object, Object>) s -> {
                String input =
                        (String) s;

                int upper =
                        input.length();

                StringBuilder result =
                        new StringBuilder(upper);

                int lp = upper - 1;
                while (lp >= 0) {
                    result.append(input.charAt(lp));
                    lp -= 1;
                }

                return result.toString();
            };
}
