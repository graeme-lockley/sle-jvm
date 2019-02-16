package za.co.no9.sle.actors;


import java.util.Collections;
import java.util.List;

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


    @Override
    protected S state() {
        return state;
    }


    @Override
    protected List<Cmd> cmds() {
        return Collections.singletonList(cmd);
    }


    @Override
    public String toString() {
        return "StateMsgResponse{" +
                "state=" + state +
                ", cmd=" + cmd +
                '}';
    }
}
