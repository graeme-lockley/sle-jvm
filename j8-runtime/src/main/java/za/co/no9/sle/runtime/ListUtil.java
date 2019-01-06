package za.co.no9.sle.runtime;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    static final int NilSelector =
            0;

    static final int ConsSelector =
            1;


    static final Object[] nil =
            {NilSelector};


    static final Object[] cons(Object a, Object b) {
        return new Object[]{ConsSelector, a, b};
    }


    static final Object arrayToList(Object[] items) {
        Object result =
                ListUtil.nil;

        int lp =
                items.length - 1;

        while (lp >= 0) {
            result = ListUtil.cons(items[lp], result);
            lp -= 1;
        }

        return result;
    }


    public static Object javaListToList(List<Integer> items) {
        Object result =
                ListUtil.nil;

        int lp =
                items.size() - 1;

        while (lp >= 0) {
            result = ListUtil.cons(items.get(lp), result);
            lp -= 1;
        }

        return result;
    }


    public static List sleListToList(Object[] sleList) {
        Object[] cell =
                sleList;

        List result =
                new ArrayList<>();

        while ((int) cell[0] == ListUtil.ConsSelector) {
            result.add(cell[1]);
            cell = (Object[]) cell[2];
        }

        return result;
    }
}
