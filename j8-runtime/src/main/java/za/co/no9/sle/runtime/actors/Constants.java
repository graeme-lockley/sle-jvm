package za.co.no9.sle.runtime.actors;

import za.co.no9.sle.actors.Cmd;
import za.co.no9.sle.actors.UpdateResult;

import java.util.ArrayList;

public class Constants {
    public static final ArrayList<Cmd> EMPTY_CMD_LIST =
            new ArrayList<>();

    public static final UpdateResult<Object> OBJECT_UPDATE_RESULT =
            new UpdateResult<>(null, EMPTY_CMD_LIST);
}
