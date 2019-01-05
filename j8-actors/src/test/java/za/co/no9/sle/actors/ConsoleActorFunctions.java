package za.co.no9.sle.actors;

import java.util.ArrayList;

public class ConsoleActorFunctions implements ActorFunction<Object, String> {
    private static final UpdateResult<Object> RESULT =
            new UpdateResult<>(null, new ArrayList<>());


    public static final ConsoleActorFunctions INSTANCE =
            new ConsoleActorFunctions();


    @Override
    public UpdateResult<Object> init(ActorRef<Object, String> self) {
        return RESULT;
    }

    @Override
    public UpdateResult<Object> update(Object state, String message) {
        System.out.println(message);

        return RESULT;
    }
}
