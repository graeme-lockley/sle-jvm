package test;

import java.util.function.Function;

public class First {

    /**
     * factorial: Int -> Int
     */
    public static final Function<Integer, Integer> factorial = new Function<Integer, Integer>() {

        public Integer apply(Integer n) {
            return EQUAL_EQUAL.apply(n).apply(0) ? 1 : STAR.apply(n).apply(factorial.apply(MINUS.apply(n).apply(1)));
        }
    };
}
