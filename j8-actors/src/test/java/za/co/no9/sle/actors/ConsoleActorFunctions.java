package za.co.no9.sle.actors;

import java.util.ArrayList;

public class ConsoleActorFunctions implements ActorFunction<Object, String> {
    private static final InitResult<Object> RESULT =
            new InitResult<>(null, new ArrayList<>());


    public static final ConsoleActorFunctions INSTANCE =
            new ConsoleActorFunctions();


    @Override
    public InitResult<Object> init(ActorRef<Object, String> self) {
        return RESULT;
    }


    @Override
    public Response<Object> update(Object state, String message) {
        System.out.println(message);

        return NoneResponse.INSTANCE;
    }
}
