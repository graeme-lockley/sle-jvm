package za.co.no9.sle.actors;


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
}
