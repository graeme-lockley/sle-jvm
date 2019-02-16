package za.co.no9.sle.actors;


import java.util.List;

import static java.util.Collections.EMPTY_LIST;

public class StateResponse<S> extends Response<S> {
    public final S state;


    public StateResponse(S state) {
        this.state = state;
    }


    @Override
    S process(S def) {
        return state;
    }


    @Override
    protected S state() {
        return state;
    }


    @Override
    protected List<Cmd> cmds() {
        return (List<Cmd>) EMPTY_LIST;
    }


    @Override
    public String toString() {
        return "StateResponse{" +
                "state=" + state +
                '}';
    }
}
