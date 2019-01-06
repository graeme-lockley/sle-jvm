package za.co.no9.sle.actors;


public class StateResponse<S> extends Response<S> {
    public final S state;


    public StateResponse(S state) {
        this.state = state;
    }


    @Override
    S process(S def) {
        return state;
    }
}
