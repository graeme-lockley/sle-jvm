package za.co.no9.sle.actors;


import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class Mailbox {
    private java.util.concurrent.BlockingDeque<Cmd> queue;


    public Mailbox() {
        queue = new LinkedBlockingDeque<>();

        new Thread(new MailboxConsumer(queue)).start();
    }


    public <S, M> void postMessage(Cmd<S, M> cmd) {
        queue.add(cmd);
    }


    public boolean hasPendingCommands() {
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
                queue.take().process();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}