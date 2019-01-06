package za.co.no9.sle.actors;

import java.util.List;

public abstract class Response<S> {
    abstract S process(S def);


    static void postCommands(List<Cmd> cmds) {
        cmds.forEach(Response::postCommand);
    }


    static void postCommand(Cmd cmd) {
        cmd.post();
    }
}
