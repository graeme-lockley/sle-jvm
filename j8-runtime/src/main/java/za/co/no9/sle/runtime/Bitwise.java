package za.co.no9.sle.runtime;

import java.util.function.Function;


public class Bitwise {
    public static final Object and =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) & ((int) b);

    public static final Object or =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) | ((int) b);

    public static final Object xor =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) ^ ((int) b);

    public static final Object complement =
            (Function<Object, Object>) a -> ~((int) a);

    public static final Object shiftLeftBy =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) b) << ((int) a);

    public static final Object shiftRightBy =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) b) >> ((int) a);

    public static final Object shiftRightZfBy =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) b) >>> ((int) a);
}
