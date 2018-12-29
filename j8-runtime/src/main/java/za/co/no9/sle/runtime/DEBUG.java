package za.co.no9.sle.runtime;

import java.util.function.Function;

public class DEBUG {
    public static final Object info =
            (Function<Object, Object>) message -> (Function<Object, Object>) value -> {
                System.out.print("info: ");
                System.out.print(message);
                System.out.print(": ");
                System.out.println(valueToString(value));

                return value;
            };


    public static final Object error =
            (Function<Object, Object>) message -> (Function<Object, Object>) value -> {
                System.err.print("error: ");
                System.err.print(message);
                System.err.print(": ");
                System.err.println(valueToString(value));

                return value;
            };


    private static String valueToString(Object value) {
        if (value instanceof Object[]) {
            StringBuilder result =
                    new StringBuilder();

            Object[] aArray =
                    (Object[]) value;

            int aLength =
                    aArray.length;

            result.append('[');
            for (int lp = 0; lp < aLength; lp += 1) {
                if (lp > 0) {
                    result.append(", ");
                }
                result.append(valueToString(aArray[lp]));
            }
            result.append(']');

            return result.toString();
        } else {
            return value.toString();
        }
    }
}
