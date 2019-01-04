package za.co.no9.sle.actors;


import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;


public class Mailbox {
    private java.util.concurrent.BlockingDeque<Cmd> queue;


    public Mailbox() {
        queue = new LinkedBlockingDeque<>();

        new Thread(new MailboxConsumer(queue)).start();
    }


    public <S, M> void postMessage(Cmd<S, M> cmd) {
        queue.add(cmd);
    }


    public boolean pendingCommands() {
        return !queue.isEmpty();
    }
}


class MailboxConsumer implements Runnable {
    private BlockingDeque<Cmd> queue;

    MailboxConsumer(java.util.concurrent.BlockingDeque<Cmd> queue) {
        this.queue = queue;
    }


    @Override
    public void run() {
        try {
            while (true) {
                Cmd cmd =
                        queue.take();

                ActorState actorState =
                        cmd.actorRef.actorState;

                Function update =
                        actorState.update;

                actorState.setState(((Function<Object, Object>) update.apply(actorState.getState())).apply(cmd.message));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}