package za.co.no9.sle.runtime;

public class Maybe {
    static final int NothingSelector =
            0;

    static final int JustSelector =
            1;


    static final Object[] nothing =
            {NothingSelector};

    static final Object[] just(Object a) {
        return new Object[]{JustSelector, a};
    }
}
