package za.co.no9.sle.runtime;

import java.util.function.Function;

public class CharUtil {
    public static final Object toCode =
            (Function<Object, Object>) a -> (int) ((char) a);

    public static final Object fromCode =
            (Function<Object, Object>) a -> (char) ((int) a);
}
