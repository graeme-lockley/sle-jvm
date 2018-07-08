package za.co.no9.sle.runtime;

import java.util.function.Function;


public class Builtin {
    public static final Function<Integer, Function<Integer, Boolean>> EQUAL_EQUAL =
            a -> a::equals;

    public static final Function<Integer, Function<Integer, Integer>> STAR =
            a -> b -> a * b;

    public static final Function<Integer, Function<Integer, Integer>> MINUS =
            a -> b -> a - b;
}
