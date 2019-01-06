package za.co.no9.sle.runtime.actors;

import za.co.no9.sle.actors.*;

public class ConsoleActor implements ActorFunction<Object, String> {
    @Override
    public InitResult<Object> init(ActorRef<Object, String> self) {
        return Constants.OBJECT_UPDATE_RESULT;
    }

    @Override
    public Response<Object> update(Object state, String message) {
        System.out.println(message);

        return NoneResponse.INSTANCE;
    }
}
