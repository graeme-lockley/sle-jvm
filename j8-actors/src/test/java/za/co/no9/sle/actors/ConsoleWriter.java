package za.co.no9.sle.actors;

import java.util.ArrayList;
import java.util.function.Function;

public class ConsoleWriter {
    public static Function<ActorRef<Object, String>, UpdateResult<Object>> init =
            self -> new UpdateResult<>(null, new ArrayList<>());


    public static Function<Object, Function<String, UpdateResult<Object>>> println = s -> m -> {
        System.out.println(m);

        return new UpdateResult<>(null, new ArrayList<>());
    };
}
