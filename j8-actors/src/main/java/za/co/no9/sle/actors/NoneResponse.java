package za.co.no9.sle.actors;


import java.util.List;

import static java.util.Collections.EMPTY_LIST;

public class NoneResponse<S> extends Response<S> {
    public static final NoneResponse INSTANCE =
            new NoneResponse<>();


    @Override
    S process(S def) {
        return def;
    }


    @Override
    protected S state() {
        return null;
    }

    @Override
    protected List<Cmd> cmds() {
        return (List<Cmd>) EMPTY_LIST;
    }


    @Override
    public String toString() {
        return "NoneResponse{}";
    }
}
