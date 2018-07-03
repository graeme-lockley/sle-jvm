package test;

import java.util.function.Function;

import static za.co.no9.sle.runtime.Operators.*;


public class Firsty {
    /**
     * factorial: Integer -> Integer
     */
    public static final Function<Integer, Integer> factorial = new Function<Integer, Integer>() {

        public Integer apply(Integer n) {
            return EQUAL_EQUAL.apply(n).apply(0) ? 1 : STAR.apply(n).apply(factorial.apply(MINUS.apply(n).apply(1)));
        }
    };


    public static void main(String args[]) {
        System.out.println(factorial.apply(5));
    }
}

