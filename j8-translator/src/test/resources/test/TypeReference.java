package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class TypeReference {

    /**
     * equal: <0> '0 -> '0 -> Bool
     */
    public static final Function<Object, Function<Object, Boolean>> equal = new Function<Object, Function<Object, Boolean>>() {

        public Function<Object, Boolean> apply(Object a) {
            return new Function<Object, Boolean>() {

                public Boolean apply(Object b) {
                    return EQUAL_EQUAL.apply(a).apply(b);
                }
            };
        }
    };
}
