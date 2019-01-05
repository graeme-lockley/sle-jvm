package za.co.no9.sle.runtime.actors;

import za.co.no9.sle.actors.ActorFunction;
import za.co.no9.sle.actors.ActorRef;
import za.co.no9.sle.actors.UpdateResult;

public class ConsoleActor implements ActorFunction<Object, String> {

    @Override
    public UpdateResult<Object> init(ActorRef<Object, String> self) {
        return Constants.OBJECT_UPDATE_RESULT;
    }

    @Override
    public UpdateResult<Object> update(Object state, String message) {
        System.out.println(message);

        return Constants.OBJECT_UPDATE_RESULT;
    }
}
