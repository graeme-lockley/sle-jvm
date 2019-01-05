package za.co.no9.sle.actors;


public class Controller {
    private int nextMailBox = 0;

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


    synchronized private Mailbox selectMailbox() {
        nextMailBox = (nextMailBox + 1) % mailboxes.length;
//        System.out.println("selected queue: " + nextMailBox);

        return mailboxes[nextMailBox];
    }


//    private Mailbox selectMailbox() {
//        final int index = (int) Math.floor(Math.random() * mailboxes.length);
//        System.out.println("selected queue: " + index);
//
//        return mailboxes[index];
//    }
//
//

    private boolean pendingCommands() {
        StringBuilder sb =
                new StringBuilder();

        boolean result =
                false;

        for (Mailbox mailbox : mailboxes) {
            result = result || mailbox.hasPendingCommands(sb);
//            || result;
        }

//        System.out.println(sb.toString());

        return result;
    }


    public void synchronousWait(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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