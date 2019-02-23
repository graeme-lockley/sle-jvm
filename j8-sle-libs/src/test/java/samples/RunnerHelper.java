package samples;


import io.kotlintest.specs.AbstractFunSpec;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class RunnerHelper {

    public static Function<Object, Object> addUnit =
            new Function<Object, Object>() {
                @Override
                public Object apply(Object o) {
                    return ADDUNIT.invoke(ROOT, o);
                }
            };

    public static Function<Object, Object> addSuite =
            new Function<Object, Object>() {
                @Override
                public Object apply(Object o) {
                    return ADDSUITE.invoke(ROOT, o);
                }
            };


    @NotNull
    public static Function2<AbstractFunSpec, Object, Unit> ADDUNIT;

    @NotNull
    public static Function2<AbstractFunSpec, Object, Unit> ADDSUITE;

    @NotNull
    public static AbstractFunSpec ROOT;
}
