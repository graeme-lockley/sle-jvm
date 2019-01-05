package za.co.no9.sle.actors;


import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class Mailbox {
    private enum Colour {GREEN, RED}

    private java.util.concurrent.BlockingDeque<Cmd> queue;

    private Colour colour;


    public Mailbox() {
        queue = new LinkedBlockingDeque<>();
        colour = Colour.GREEN;

        new Thread(new MailboxConsumer(queue)).start();
    }


    public <S, M> void postMessage(Cmd<S, M> cmd) {
        queue.add(cmd);
        colour = Colour.RED;
    }


    public boolean hasPendingCommands(StringBuilder sb) {
        Colour mailboxColour =
                colour;

//        int queueSize =
//                queue.size();

//        sb.append(':').append(queueSize).append(mailboxColour == Colour.RED ? "X" : "-");


//        boolean busy =
//                queueSize > 0 || mailboxColour == Colour.RED;
        boolean busy =
                !queue.isEmpty() || mailboxColour == Colour.RED;

        colour = Colour.GREEN;

        return busy;
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