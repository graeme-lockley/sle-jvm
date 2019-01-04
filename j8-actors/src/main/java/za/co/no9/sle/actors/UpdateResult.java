package za.co.no9.sle.actors;

import java.util.List;

public class UpdateResult<S> {
    public final S state;
    public final List<Cmd> commands;


    public UpdateResult(S state, List<Cmd> commands) {
        this.state = state;
        this.commands = commands;
    }
}
