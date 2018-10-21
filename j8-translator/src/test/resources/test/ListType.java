package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class ListType {

    public static final int Nil$ = 0;

    /**
     * Nil: <0> List '0
     */
    public static final Object Nil = new Object[] { Nil$ };

    public static final int Cons$ = 1;

    /**
     * Cons: <0> '0 -> List '0 -> List '0
     */
    public static final Function<Object, Function<Object, Object>> Cons = new Function<Object, Function<Object, Object>>() {

        public Function<Object, Object> apply(Object v0) {
            return new Function<Object, Object>() {

                public Object apply(Object v1) {
                    return new Object[] { Cons$, v0, v1 };
                }
            };
        }
    };

    /**
     * singleton: <0> '0 -> List '0
     */
    public static final Function<Object, Object> singleton = new Function<Object, Object>() {

        public Object apply(Object x) {
            return Cons.apply(x).apply(Nil);
        }
    };

    /**
     * double: <0> '0 -> '0 -> List '0
     */
    public static final Function<Object, Function<Object, Object>> double = new Function<Object, Function<Object, Object>>() {

        public Function<Object, Object> apply(Object x) {
            return new Function<Object, Object>() {

                public Object apply(Object y) {
                    return Cons.apply(x).apply(singleton.apply(y));
                }
            };
        }
    };

    /**
     * singleIntList: <> List Int
     */
    public static final Object singleIntList = singleton.apply(10);

    /**
     * doubleIntList: <> List Int
     */
    public static final Object doubleIntList = double.apply(1).apply(2);

    /**
     * doubleBooleanList: <> List Bool
     */
    public static final Object doubleBooleanList = double.apply(true).apply(false);
}
