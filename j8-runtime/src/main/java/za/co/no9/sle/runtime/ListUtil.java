package za.co.no9.sle.runtime;

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
}
