package za.co.no9.sle.actors;


import java.util.List;
import java.util.function.Function;

public class Controller {
    private Mailbox[] mailboxes;


    public Controller(int numberOfMailboxes) {
        mailboxes = new Mailbox[numberOfMailboxes];

        for (int lp = 0; lp < numberOfMailboxes; lp += 1) {
            mailboxes[lp] = new Mailbox();
        }
    }


    public <S, M> ActorRef<S, M> create(Function<ActorRef<S, M>, UpdateResult<S>> init, Function<S, Function<M, UpdateResult<S>>> update) {
        Mailbox mailbox = selectMailbox();

        ActorState<S, M> actorState = new ActorState<>(update, null);
        ActorRef<S, M> actorRef = new ActorRef<>(actorState, mailbox);

        UpdateResult<S> initResult = init.apply(actorRef);

        actorState.setState(initResult.state);

        scheduleCommands(initResult.commands);

        return actorRef;
    }


    public void scheduleCommands(List<Cmd> commands) {
        commands.forEach(cmd -> cmd.actorRef.postMessage(cmd));
    }


    private Mailbox selectMailbox() {
        return mailboxes[(int) Math.floor(Math.random() * mailboxes.length)];
    }

    private boolean pendingCommands() {
        for (int lp = 0; lp < mailboxes.length; lp += 1) {
            if (mailboxes[lp].pendingCommands()) {
                System.out.println("pendingCommands: " + lp);
                return true;
            }
        }

        return false;
    }


    public void synchronousWait(long duration) {
        while (true) {
            if (!pendingCommands()) {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!pendingCommands()) {
                    return;
                }
            } else {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}