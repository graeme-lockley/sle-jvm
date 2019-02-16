package za.co.no9.sle.actors;


import java.util.List;

public class StateMsgsResponse<S> extends Response<S> {
    public final S state;
    public final List<Cmd> cmds;


    public StateMsgsResponse(S state, List<Cmd> cmds) {
        this.state = state;
        this.cmds = cmds;
    }


    @Override
    S process(S def) {
        postCommands(cmds);
        return state;
    }


    @Override
    protected S state() {
        return state;
    }

    @Override
    protected List<Cmd> cmds() {
        return cmds;
    }
}
