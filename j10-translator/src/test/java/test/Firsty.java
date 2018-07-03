package test;

import java.util.function.Function;

public class Firsty {
    /**
     * factorial: Integer -> Integer
     */
    public static final Function<Integer, Integer> factorial = new Function<Integer, Integer>() {

        public Integer apply(Integer n) {
            return EQUAL_EQUAL.apply(n).apply(0) ? 1 : STAR.apply(n).apply(factorial.apply(MINUS.apply(n).apply(1)));
        }
    };


    public static final Function<Integer, Function<Integer, Boolean>> EQUAL_EQUAL =
            new Function<Integer, Function<Integer, Boolean>>() {
                @Override
                public Function<Integer, Boolean> apply(Integer a) {
                    return new Function<Integer, Boolean>() {
                        @Override
                        public Boolean apply(Integer b) {
                            return a.equals(b);
                        }
                    };
                }
            };


    public static final Function<Integer, Function<Integer, Integer>> STAR =
            new Function<Integer, Function<Integer, Integer>>() {
                @Override
                public Function<Integer, Integer> apply(Integer a) {
                    return new Function<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer b) {
                            return a * b;
                        }
                    };
                }
            };

    public static final Function<Integer, Function<Integer, Integer>> MINUS =
            new Function<Integer, Function<Integer, Integer>>() {
                @Override
                public Function<Integer, Integer> apply(Integer a) {
                    return new Function<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer b) {
                            return a - b;
                        }
                    };
                }
            };


    public static void main(String args[]) {
        System.out.println(factorial.apply(5));
    }
}

