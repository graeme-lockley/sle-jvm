package za.co.no9.sle.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static za.co.no9.sle.runtime.Maybe.just;
import static za.co.no9.sle.runtime.Maybe.nothing;

public class StringUtil {
    public static final Object length =
            (Function<Object, Object>) s -> ((String) s).length();

    public static final Object at =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                int index =
                        (int) a;

                String s =
                        (String) b;

                if (index < s.length()) {
                    return Maybe.just(s.charAt(index));
                } else {
                    return nothing;
                }
            };

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

    public static final Object uncons =
            (Function<Object, Object>) a -> {
                String s =
                        (String) a;

                if (s.length() == 0) {
                    return Maybe.nothing;
                } else {
                    return Maybe.just(Tuple.tuple(s.charAt(0), s.substring(1)));
                }
            };


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

    public static final Object fromInt =
            (Function<Object, Object>) a -> Integer.toString((int) a);


    public static final Object toInt =
            (Function<Object, Object>) a -> {
                String s =
                        (String) a;

                try {
                    return just(Integer.parseInt(s));
                } catch (java.lang.NumberFormatException e) {
                    return nothing;
                }
            };

    public static final Object foldLeft =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> (Function<Object, Object>) c -> {
                Function<Object, Function<Object, Object>> f =
                        (Function<Object, Function<Object, Object>>) a;

                String s =
                        (String) c;

                Object result =
                        b;

                for (int lp = 0; lp < s.length(); lp += 1) {
                    result = f.apply(result).apply(s.charAt(lp));
                }

                return result;
            };

    public static final Object foldRight =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> (Function<Object, Object>) c -> {
                Function<Object, Function<Object, Object>> f =
                        (Function<Object, Function<Object, Object>>) a;

                String s =
                        (String) c;

                Object result =
                        b;

                int lp = s.length() - 1;
                while (lp >= 0) {
                    result = f.apply(s.charAt(lp)).apply(result);
                    lp -= 1;
                }

                return result;
            };

    public static final Object map =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Function<Object, Object> f =
                        (Function<Object, Object>) a;

                String s =
                        (String) b;

                int sLength =
                        s.length();

                StringBuilder result =
                        new StringBuilder(s.length());

                for (int lp = 0; lp < sLength; lp += 1) {
                    result.append((char) f.apply(s.charAt(lp)));
                }

                return result.toString();
            };

    public static final Object filter =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Function<Object, Object> f =
                        (Function<Object, Object>) a;

                String s =
                        (String) b;

                int sLength =
                        s.length();

                StringBuilder result =
                        new StringBuilder();

                for (int lp = 0; lp < sLength; lp += 1) {
                    char ch =
                            s.charAt(lp);

                    if ((boolean) f.apply(ch)) {
                        result.append(ch);
                    }
                }

                return result.toString();
            };
}
