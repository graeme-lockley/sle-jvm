package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class First {

    /**
     * factorial: <> Data.Int -> Data.Int
     */
    public static final Function<Object, Object> factorial = new Function<Object, Object>() {

        public Object apply(Object n) {
            return EQUAL_EQUAL.apply(n).apply(0) ? 1 : STAR.apply(n).apply(factorial.apply(MINUS.apply(n).apply(1)));
        }
    };

    /**
     * factorial2: <> Data.Int -> Data.Int
     */
    public static final Function<Object, Object> factorial2 = new Function<Object, Object>() {

        public Object apply(Object n) {
            return EQUAL_EQUAL.apply(n).apply(0) ? 1 : STAR.apply(n).apply(factorial2.apply(MINUS.apply(n).apply(1)));
        }
    };

    /**
     * factorial3: <> Data.Int -> Data.Int
     */
    public static final Function<Object, Object> factorial3 = new Function<Object, Object>() {

        public Object apply(Object $v0) {
            return EQUAL_EQUAL.apply(0).apply($v0) ? 1 : STAR.apply($v0).apply(factorial3.apply(MINUS.apply($v0).apply(1)));
        }
    };
}
