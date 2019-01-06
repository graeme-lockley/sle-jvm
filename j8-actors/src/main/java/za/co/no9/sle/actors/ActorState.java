package za.co.no9.sle.actors;


public class ActorState<S, M> {
    private ActorFunction<S, M> functions;

    private S state;


    public ActorState(ActorFunction<S, M> functions, S state) {
        this.functions = functions;
        this.state = state;
    }


    public void init(ActorRef<S, M> self) {
        InitResult<S> result =
                functions.init(self);

        Response.postCommands(result.commands);
        state = result.state;
    }


    public void update(M message) {
        Response<S> response =
                functions.update(state, message);

        state = response.process(state);
    }
}