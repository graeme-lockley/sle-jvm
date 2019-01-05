package za.co.no9.sle.actors;


public class ActorRef<S, M> {
    ActorState<S, M> actorState;

    private Mailbox mailbox;


    public ActorRef(ActorState actorState, Mailbox mailbox) {
        this.actorState = actorState;
        this.mailbox = mailbox;
    }


    void postMessage(Cmd<S, M> cmd) {
        mailbox.postMessage(cmd);
    }


    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}