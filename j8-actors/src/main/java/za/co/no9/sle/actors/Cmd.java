package za.co.no9.sle.actors;

public class Cmd<S, M> {
    public final ActorRef<S, M> actorRef;
    public final M message;


    public Cmd(ActorRef<S, M> actorRef, M message) {
        this.actorRef = actorRef;
        this.message = message;
    }

    public void post() {
        actorRef.postMessage(this);
    }

    public void process() {
        actorRef.actorState.update(message);
    }

    @Override
    public String toString() {
        return "Cmd{" +
                "actorRef=" + actorRef +
                ", message=" + message +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Cmd){
            Cmd cmdO = (Cmd) o;

            return actorRef.equals(cmdO.actorRef) && equals(message, cmdO.message);
        } else {
            return false;
        }
    }


    private static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        } else if (a instanceof Object[] && b instanceof Object[]) {
            Object[] aArray =
                    (Object[]) a;

            Object[] bArray =
                    (Object[]) b;

            int aLength =
                    aArray.length;

            if (aLength == bArray.length) {
                for (int lp = 0; lp < aLength; lp += 1) {
                    if (!equals(aArray[lp], bArray[lp])) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return a.equals(b);
        }
    }
}
