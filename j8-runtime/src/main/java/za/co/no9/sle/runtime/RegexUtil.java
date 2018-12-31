package za.co.no9.sle.runtime;

import java.util.function.Function;
import java.util.regex.Pattern;

public class RegexUtil {
    public static final Object fromString =
            (Function<Object, Object>) a -> {
                String regex =
                        (String) a;

                System.out.println("[" + regex + "]" + "  " + regex.length());

                try {
                    return Maybe.just(Pattern.compile(regex));
                } catch (Exception e) {
                    return Maybe.nothing;
                }
            };

    public static final Object never =
            Pattern.compile("\\a{1000000}");

    public static final Object quote =
            (Function<Object, Object>) a -> {
                String text =
                        (String) a;

                return Pattern.quote(text);
            };

    public static final Object contains =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Pattern pattern =
                        (Pattern) a;

                String text =
                        (String) b;

                return pattern.matcher(text).matches();
            };

    public static final Object split =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Pattern pattern =
                        (Pattern) a;

                String text =
                        (String) b;

                return ListUtil.arrayToList(pattern.split(text));
            };

}
