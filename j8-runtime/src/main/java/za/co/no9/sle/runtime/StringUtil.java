package za.co.no9.sle.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class StringUtil {
    public static final Object length =
            (Function<Object, Object>) s -> ((String) s).length();

    public static final Object reverse =
            (Function<Object, Object>) s -> {
                String input =
                        (String) s;

                int upper =
                        input.length();

                StringBuilder result =
                        new StringBuilder(upper);

                int lp = upper - 1;
                while (lp >= 0) {
                    result.append(input.charAt(lp));
                    lp -= 1;
                }

                return result.toString();
            };

    public static final Object append =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((String) a) + ((String) b);

    public static final Object join =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Object[] lst =
                        (Object[]) b;

                if (((int) lst[0]) == ListUtil.NilSelector) {
                    return "";
                } else {
                    String sep =
                            (String) a;

                    StringBuilder result =
                            new StringBuilder();

                    result.append((String) lst[1]);
                    lst = (Object[]) lst[2];
                    while (((int) lst[0]) == ListUtil.ConsSelector) {
                        result.append(sep);
                        result.append((String) lst[1]);
                        lst = (Object[]) lst[2];
                    }

                    return result.toString();
                }
            };

    public static final Object split =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                String sep =
                        (String) a;

                String text =
                        (String) b;

                return ListUtil.arrayToList(text.split(Pattern.quote(sep)));
            };

    public static final Object slice =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> (Function<Object, Object>) c -> {
                String text =
                        (String) c;

                int textLength =
                        text.length();

                int startIndex =
                        calculateIndex((int) a, textLength);

                int endIndex =
                        Math.max(startIndex, calculateIndex((int) b, textLength));

                return text.substring(startIndex, endIndex);
            };

    private static int calculateIndex(int index, int textLength) {
        if (index < 0) {
            int newIndex =
                    textLength + index;

            return newIndex < 0 ? 0 : newIndex;
        } else {
            return index > textLength ? textLength : index;
        }
    }

    public static final Object contains =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((String) b).contains((String) a);

    public static final Object startsWith =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((String) b).startsWith((String) a);

    public static final Object endsWith =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> ((String) b).endsWith((String) a);

    public static final Object indexes =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                String needle =
                        (String) a;

                String haystack =
                        (String) b;

                int haystackSize =
                        haystack.length();

                List<Integer> result =
                        new ArrayList<>();

                int index = haystack.indexOf(needle);
                while (index >= 0 && index < haystackSize) {
                    result.add(index);
                    index = haystack.indexOf(needle, index + 1);
                }

                return ListUtil.javaListToList(result);
            };

    public static final Object toUpper =
            (Function<Object, Object>) a -> ((String) a).toUpperCase();

    public static final Object toLower =
            (Function<Object, Object>) a -> ((String) a).toLowerCase();

    public static final Object cons =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> Character.toString((char) a) + ((String) b);

    public static final Object trim =
            (Function<Object, Object>) a -> ((String) a).trim();

    public static final Object trimLeft =
            (Function<Object, Object>) a -> {
                String s =
                        (String) a;

                int sLength =
                        s.length();

                int index =
                        0;

                while (true) {
                    if (index == sLength) {
                        return "";
                    } else if (Character.isWhitespace(s.charAt(index))) {
                        index += 1;
                    } else if (index == 0) {
                        return s;
                    } else {
                        return s.substring(index);
                    }
                }
            };

    public static final Object trimRight =
            (Function<Object, Object>) a -> {
                String s =
                        (String) a;

                int sLength =
                        s.length();

                if (sLength == 0) {
                    return s;
                } else {
                    int index =
                            sLength - 1;

                    while (true) {
                        if (Character.isWhitespace(s.charAt(index))) {
                            if (index == 0) {
                                return "";
                            } else {
                                index -= 1;
                            }
                        } else {
                            return s.substring(0, index + 1);
                        }
                    }
                }
            };
}
