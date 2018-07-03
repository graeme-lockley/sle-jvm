package test;


import java.util.function.Function;

public class First {
    /**
     * factorial: Integer -> Integer
     */
    public static final Function<Integer, Integer> factorial =
            new Function<Integer, Integer>() {
                public Integer apply(Integer n) {
                    if (n == 0) {
                        return 1;
                    } else {
                        return n * factorial.apply(n - 1);
                    }
                }
            };
}