package za.co.no9.sle.actors;


import java.util.Collections;
import java.util.List;

public class MsgResponse<S> extends Response<S> {
    public final Cmd cmd;


    public MsgResponse(Cmd cmd) {
        this.cmd = cmd;
    }


    @Override
    S process(S def) {
        postCommand(cmd);
        return def;
    }


    @Override
    protected S state() {
        return null;
    }


    @Override
    protected List<Cmd> cmds() {
        return Collections.singletonList(cmd);
    }
}
