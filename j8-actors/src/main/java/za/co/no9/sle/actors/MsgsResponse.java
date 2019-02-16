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


    @Override
    protected S state() {
        return null;
    }


    @Override
    protected List<Cmd> cmds() {
        return cmds;
    }


    @Override
    public String toString() {
        return "MsgsResponse{" +
                "cmds=" + cmds +
                '}';
    }
}
