package za.co.no9.sle.actors;


import java.util.List;

public class MsgsResponse<S> extends Response<S> {
    public final List<Cmd> cmds;


    public MsgsResponse(List<Cmd> cmds) {
        this.cmds = cmds;
    }


    @Override
    S process(S def) {
        postCommands(cmds);
        return def;
    }
}
