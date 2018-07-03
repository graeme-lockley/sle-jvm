package za.co.no9.sle.runtime;

import java.util.function.Function;


public class Operators {
    public static final Function<Integer, Function<Integer, Boolean>> EQUAL_EQUAL =
            a -> (Function<Integer, Boolean>) a::equals;

    public static final Function<Integer, Function<Integer, Integer>> STAR =
            a -> (Function<Integer, Integer>) b -> a * b;

    public static final Function<Integer, Function<Integer, Integer>> MINUS =
            a -> (Function<Integer, Integer>) b -> a - b;
}
