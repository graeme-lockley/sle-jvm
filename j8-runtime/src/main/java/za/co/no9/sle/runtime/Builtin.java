package za.co.no9.sle.runtime;

import java.util.function.Function;


public class Builtin {
    public static final Function<Object, Function<Object, Boolean>> AMPERSAND_AMPERSAND =
            a -> b -> ((boolean) a) && ((boolean) b);

    public static final Function<Object, Function<Object, Boolean>> BAR_BAR =
            a -> b -> ((boolean) a) || ((boolean) b);

    public static final Function<Object, Function<Object, Boolean>> EQUAL_EQUAL =
            a -> (Function<Object, Boolean>) a::equals;

    public static final Function<Object, Function<Object, Boolean>> BANG_EQUAL =
            a -> (Function<Object, Boolean>) b -> !a.equals(b);

    public static final Function<Object, Function<Object, Boolean>> LESS =
            a -> (Function<Object, Boolean>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) < ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) < 0
                            : (a instanceof Boolean && b instanceof Boolean) && ((Boolean) a).compareTo((Boolean) b) < 0;

    public static final Function<Object, Function<Object, Boolean>> LESS_EQUAL =
            a -> (Function<Object, Boolean>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) <= ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) <= 0
                            : a instanceof Boolean && b instanceof Boolean ? ((Boolean) a).compareTo((Boolean) b) <= 0
                            : a instanceof Unit && b instanceof Unit;

    public static final Function<Object, Function<Object, Boolean>> GREATER =
            a -> (Function<Object, Boolean>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) > ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) > 0
                            : (a instanceof Boolean && b instanceof Boolean) && ((Boolean) a).compareTo((Boolean) b) > 0;

    public static final Function<Object, Function<Object, Boolean>> GREATER_EQUAL =
            a -> (Function<Object, Boolean>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) >= ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) >= 0
                            : a instanceof Boolean && b instanceof Boolean ? ((Boolean) a).compareTo((Boolean) b) >= 0
                            : a instanceof Unit && b instanceof Unit;

    public static final Function<Object, Function<Object, Object>> PLUS =
            a -> b -> ((int) a) + ((int) b);

    public static final Function<Object, Function<Object, Object>> STAR =
            a -> b -> ((int) a) * ((int) b);

    public static final Function<Object, Function<Object, Object>> SLASH =
            a -> b -> ((int) a) / ((int) b);

    public static final Function<Object, Function<Object, Object>> MINUS =
            a -> b -> ((int) a) - ((int) b);
}
