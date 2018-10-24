package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class UnitValue {

    /**
     * calculate: <> () -> Int
     */
    public static final Function<Object, Object> calculate = new Function<Object, Object>() {

        public Object apply(Object n) {
            return 100;
        }
    };

    /**
     * value: <> Int
     */
    public static final Object value = calculate.apply(za.co.no9.sle.runtime.Unit.INSTANCE);
}
