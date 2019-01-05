package za.co.no9.sle.actors;


import java.util.List;

public class ActorState<S, M> {
    private ActorFunction<S, M> functions;

    private S state;


    public ActorState(ActorFunction<S, M> functions, S state) {
        this.functions = functions;
        this.state = state;
    }


    public void init(ActorRef<S, M> self) {
        processUpdateResult(functions.init(self));
    }


    public void update(M message) {
        processUpdateResult(functions.update(state, message));
    }


    private void processUpdateResult(UpdateResult<S> result) {
        state = result.state;
        postCommands(result.commands);
    }


    private void postCommands(List<Cmd> cmds) {
        cmds.forEach(cmd -> cmd.post());
    }
}