package za.co.no9.sle.actors;


public class Controller {
    private Mailbox[] mailboxes;


    public Controller(int numberOfMailboxes) {
        mailboxes = new Mailbox[numberOfMailboxes];

        for (int lp = 0; lp < numberOfMailboxes; lp += 1) {
            mailboxes[lp] = new Mailbox();
        }
    }


    public <S, M> ActorRef<S, M> create(ActorFunction<S, M> functions) {
        Mailbox mailbox = selectMailbox();

        ActorState<S, M> actorState = new ActorState<>(functions, null);
        ActorRef<S, M> actorRef = new ActorRef<>(actorState, mailbox);

        actorState.init(actorRef);

        return actorRef;
    }


    private Mailbox selectMailbox() {
        return mailboxes[(int) Math.floor(Math.random() * mailboxes.length)];
    }

    private boolean pendingCommands() {
        for (int lp = 0; lp < mailboxes.length; lp += 1) {
            if (mailboxes[lp].hasPendingCommands()) {
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