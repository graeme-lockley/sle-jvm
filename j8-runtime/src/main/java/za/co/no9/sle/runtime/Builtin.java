package za.co.no9.sle.runtime;

import java.util.function.Function;


public class Builtin {
    public static final Function<Object, Function<Object, Boolean>> EQUAL_EQUAL =
            a -> (Function<Object, Boolean>) a::equals;

    public static final Function<Object, Function<Object, Boolean>> BANG_EQUAL =
            a -> (Function<Object, Boolean>) b -> !a.equals(b);

    public static final Function<Integer, Function<Integer, Integer>> STAR =
            a -> b -> a * b;

    public static final Function<Integer, Function<Integer, Integer>> SLASH =
            a -> b -> a / b;

    public static final Function<Integer, Function<Integer, Integer>> MINUS =
            a -> b -> a - b;
}
