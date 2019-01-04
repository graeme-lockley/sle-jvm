package za.co.no9.sle.actors;


import java.util.function.Function;

public class ActorState<S, M> {
    Function<S, Function<M, UpdateResult<S>>> update;

    private S state;


    public ActorState(Function<S, Function<M, UpdateResult<S>>> update, S state) {
        this.update = update;
        this.state = state;
    }


    void setState(S state) {
        this.state = state;
    }


    S getState() {
        return state;
    }
}