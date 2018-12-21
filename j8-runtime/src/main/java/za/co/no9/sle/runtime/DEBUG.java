package za.co.no9.sle.runtime;

import java.util.function.Function;

public class DEBUG {
    public static final Object info =
            (Function<Object, Object>) message -> (Function<Object, Object>) value -> {
                System.out.print("info: ");
                System.out.print(message);
                System.out.print(": ");
                System.out.println(value);

                return value;
            };


    public static final Object error =
            (Function<Object, Object>) message -> (Function<Object, Object>) value -> {
                System.err.print("error: ");
                System.err.print(message);
                System.err.print(": ");
                System.err.println(value);

                return value;
            };
}
