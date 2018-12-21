package za.co.no9.sle.runtime;

import java.util.function.Function;
import java.util.regex.Pattern;

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

    public static final Object append =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((String) a) + ((String) b);

    public static final Object join =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Object[] lst =
                        (Object[]) b;

                if (((int) lst[0]) == ListUtil.NilSelector) {
                    return "";
                } else {
                    String sep =
                            (String) a;

                    StringBuilder result =
                            new StringBuilder();

                    result.append((String) lst[1]);
                    lst = (Object[]) lst[2];
                    while (((int) lst[0]) == ListUtil.ConsSelector) {
                        result.append(sep);
                        result.append((String) lst[1]);
                        lst = (Object[]) lst[2];
                    }

                    return result.toString();
                }
            };

    public static final Object split =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                String sep =
                        (String) a;

                String text =
                        (String) b;

                return ListUtil.arrayToList(text.split(Pattern.quote(sep)));
            };
}
