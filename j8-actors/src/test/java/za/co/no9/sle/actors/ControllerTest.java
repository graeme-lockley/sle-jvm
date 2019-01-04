package za.co.no9.sle.actors;


import org.junit.Test;

import java.util.Arrays;


public class ControllerTest {
    @Test
    public void runSomething() {
        Controller controller =
                new Controller(1000);

        ActorRef<Object, String> actorRef =
                controller.create(ConsoleWriter.init, ConsoleWriter.println);

        for (int lp = 0; lp < 1; lp += 1) {
            controller.scheduleCommands(Arrays.asList(
                    new Cmd<>(actorRef, "Hello World " + lp)
            ));
        }

        controller.synchronousWait(10);
    }
}
