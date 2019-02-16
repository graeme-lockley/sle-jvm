package za.co.no9.sle.actors;

import java.util.List;

public abstract class Response<S> {
    abstract S process(S def);

    abstract protected S state();

    abstract protected List<Cmd> cmds();

    static void postCommands(List<Cmd> cmds) {
        cmds.forEach(Response::postCommand);
    }


    static void postCommand(Cmd cmd) {
        cmd.post();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Response) {
            Response responseO = (Response) o;

            Object thisState = state();
            Object otherState = responseO.state();

            List<Cmd> thisCmds = cmds();
            List<Cmd> otherCmds = responseO.cmds();

            return equals(thisState, otherState) && equals(thisCmds, otherCmds);
        } else {
            return false;
        }
    }


    private static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a instanceof List && b instanceof List) {
            List aList = (List) a;
            List bList = (List) b;

            if (aList.size() == bList.size()) {
                int lp = 0;
                while (lp < aList.size()) {
                    if (!equals(aList.get(lp), bList.get(lp))) {
                        return false;
                    }
                    lp += 1;
                }
                return true;
            } else {
                return false;
            }
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
