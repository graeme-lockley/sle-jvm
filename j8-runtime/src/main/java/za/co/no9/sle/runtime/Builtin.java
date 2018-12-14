package za.co.no9.sle.runtime;

import java.util.function.Function;


public class Builtin {
    public static final Function<Object, Object> BUILTIN_VALUE = nameObject -> {
        String name =
                (String) nameObject;

        int lastIndexOfPeriod =
                name.lastIndexOf('.');

        if (lastIndexOfPeriod == -1) {
            throw new IllegalBuiltInNameException(name);
        }

        String className =
                name.substring(0, lastIndexOfPeriod);

        if (className.isEmpty()) {
            throw new IllegalBuiltInNameException(name);
        }

        String fieldName =
                name.substring(lastIndexOfPeriod + 1);

        if (fieldName.isEmpty()) {
            throw new IllegalBuiltInNameException(name);
        }

        try {
            Object result =
                    Class.forName(className).getDeclaredField(fieldName).get(null);

            if (result == null) {
                throw new IllegalBuiltInNameException(name, new NullPointerException());
            }

            return result;
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            throw new IllegalBuiltInNameException(name, e);
        }
    };

    public static final Object AMPERSAND_AMPERSAND =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((boolean) a) && ((boolean) b);

    public static final Object BAR_BAR =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((boolean) a) || ((boolean) b);

    public static final Object EQUAL_EQUAL =
            (Function<Object, Object>) a -> (Function<Object, Object>) a::equals;

    public static final Object BANG_EQUAL =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> !a.equals(b);

    public static final Object LESS =
            (Function<Object, Object>) a -> (Function<Object, Object>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) < ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) < 0
                            : (a instanceof Boolean && b instanceof Boolean) && ((Boolean) a).compareTo((Boolean) b) < 0;

    public static final Object LESS_EQUAL =
            (Function<Object, Object>) a -> (Function<Object, Object>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) <= ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) <= 0
                            : a instanceof Boolean && b instanceof Boolean ? ((Boolean) a).compareTo((Boolean) b) <= 0
                            : a instanceof Unit && b instanceof Unit;

    public static final Object GREATER =
            (Function<Object, Object>) a -> (Function<Object, Object>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) > ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) > 0
                            : (a instanceof Boolean && b instanceof Boolean) && ((Boolean) a).compareTo((Boolean) b) > 0;

    public static final Object GREATER_EQUAL =
            (Function<Object, Object>) a -> (Function<Object, Object>) b ->
                    a instanceof Integer && b instanceof Integer ? ((Integer) a) >= ((Integer) b)
                            : a instanceof String && b instanceof String ? ((String) a).compareTo((String) b) >= 0
                            : a instanceof Boolean && b instanceof Boolean ? ((Boolean) a).compareTo((Boolean) b) >= 0
                            : a instanceof Unit && b instanceof Unit;

    public static final Object PLUS =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) + ((int) b);

    public static final Object STAR =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) * ((int) b);

    public static final Object SLASH =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) / ((int) b);

    public static final Object MINUS =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((int) a) - ((int) b);
}
