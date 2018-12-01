package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class TypeReference {

    /**
     * equal: <0> '0 -> '0 -> Data.Bool
     */
    public static final Function<Object, Function<Object, Object>> equal = new Function<Object, Function<Object, Object>>() {

        public Function<Object, Object> apply(Object a) {
            return new Function<Object, Object>() {

                public Object apply(Object b) {
                    return EQUAL_EQUAL.apply(a).apply(b);
                }
            };
        }
    };
}
