package za.co.no9.sle.actors;


public class StateMsgResponse<S> extends Response<S> {
    public final S state;
    public final Cmd cmd;


    public StateMsgResponse(S state, Cmd cmd) {
        this.state = state;
        this.cmd = cmd;
    }


    @Override
    S process(S def) {
        postCommand(cmd);
        return state;
    }
}
