package za.co.no9.sle.actors;

public class Cmd<S, M> {
    public final ActorRef<S, M> actorRef;
    public final M message;


    public Cmd(ActorRef<S, M> actorRef, M message) {
        this.actorRef = actorRef;
        this.message = message;
    }
}
