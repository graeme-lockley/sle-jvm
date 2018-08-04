package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class UnitValue {

    /**
     * calculate: <> () -> Int
     */
    public static final Function<za.co.no9.sle.runtime.Unit, Integer> calculate = new Function<za.co.no9.sle.runtime.Unit, Integer>() {

        public Integer apply(za.co.no9.sle.runtime.Unit n) {
            return 100;
        }
    };

    /**
     * value: <> Int
     */
    public static final Integer value = calculate.apply(za.co.no9.sle.runtime.Unit.INSTANCE);
}
