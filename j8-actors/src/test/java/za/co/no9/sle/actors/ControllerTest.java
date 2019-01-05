package za.co.no9.sle.actors;


import org.junit.Test;

import static za.co.no9.sle.actors.ConsoleActorFunctions.INSTANCE;


public class ControllerTest {
    @Test
    public void runSomething() {
        Controller controller =
                new Controller(1000);

        ActorRef<Object, String> actorRef =
                controller.create(INSTANCE);

        for (int lp = 0; lp < 10; lp += 1) {
            new Cmd<>(actorRef, "Hello World " + lp).post();
        }

        controller.synchronousWait(10);
    }
}
