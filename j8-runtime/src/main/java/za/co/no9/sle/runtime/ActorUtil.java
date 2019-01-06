package za.co.no9.sle.runtime;

import za.co.no9.sle.actors.*;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class ActorUtil {
    private static Controller controller =
            new Controller(10);


    public static final Object create =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                Function<ActorRef, Object[]> init =
                        (Function<ActorRef, Object[]>) a;

                Function<Object, Function<Object, Object>> update =
                        (Function<Object, Function<Object, Object>>) b;

                return controller.create(new Delegate<>(init, update));
            };


    public static final Object cmd =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                ActorRef actorRef =
                        (ActorRef) a;

                Object message =
                        (Object) b;

                return new Cmd(actorRef, message);
            };


    public static final Object createBuiltin =
            (Function<Object, Object>) a -> {
                String builtInClassName =
                        (String) a;

                try {
                    return controller.create((ActorFunction) Class.forName(builtInClassName).getConstructor().newInstance());
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                    System.exit(1);
                    return null;
                }
            };


    public static final Object createBuiltinWithInit =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> {
                String builtInClassName =
                        (String) a;

                Function<ActorRef, Object[]> init =
                        (Function<ActorRef, Object[]>) b;

                try {
                    return controller.create(new BuiltinWithinInitDelegate<>(builtInClassName, init));
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                    System.exit(1);
                    return null;
                }
            };

    public static final Object synchronousWait =
            (Function<Object, Object>) a -> {
                int duration =
                        (int) a;

                controller.synchronousWait(duration);

                return Unit.INSTANCE;
            };


    public static final Object none =
            NoneResponse.INSTANCE;

    public static final Object state =
            (Function<Object, Object>) StateResponse::new;

    public static final Object msg =
            (Function<Object, Object>) a -> new MsgResponse<>((Cmd) a);

    public static final Object stateMsg =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> new StateMsgResponse<>(a, (Cmd) b);

    public static final Object msgs =
            (Function<Object, Object>) a -> new MsgsResponse(ListUtil.sleListToList((Object[]) a));

    public static final Object stateMsgs =
            (Function<Object, Object>) a -> (Function<Object, Object>) b -> new StateMsgsResponse(a, ListUtil.sleListToList((Object[]) b));
}


class Delegate<S, M> implements ActorFunction<S, M> {
    private final Function<ActorRef, Object[]> init;
    private final Function<Object, Function<Object, Object>> update;

    Delegate(Function<ActorRef, Object[]> init, Function<Object, Function<Object, Object>> update) {
        this.init = init;
        this.update = update;
    }


    @Override
    public InitResult<S> init(ActorRef<S, M> self) {
        return mapInitResult(init.apply(self));
    }


    @Override
    public Response<S> update(S state, M message) {
        return (Response) update.apply(state).apply(message);
    }


    private void printValue(Object object) {
        if (object instanceof Object[]) {
            Object[] value =
                    (Object[]) object;

            System.out.print('[');
            for (int lp = 0; lp < value.length; lp += 1) {
                if (lp > 0)
                    System.out.print(", ");

                printValue(value[lp]);
            }
            System.out.println("]");
        } else if (object == null) {
            System.out.print("null");
        } else {
            System.out.print(object + ": " + object.getClass().getName());
        }
    }


    private InitResult<S> mapInitResult(Object[] result) {
        return new InitResult<>(result[1], ListUtil.sleListToList((Object[]) result[2]));
    }
}


class BuiltinWithinInitDelegate<S, M> implements ActorFunction<S, M> {
    private final ActorFunction<S, M> actorFunction;
    private final Function<ActorRef, Object[]> init;


    BuiltinWithinInitDelegate(String builtInClassName, Function<ActorRef, Object[]> init) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        actorFunction = (ActorFunction<S, M>) Class.forName(builtInClassName).getConstructor().newInstance();
        this.init = init;
    }


    @Override
    public InitResult<S> init(ActorRef<S, M> self) {
        return mapResult(init.apply(self));
    }


    @Override
    public Response<S> update(S state, M message) {
        return actorFunction.update(state, message);
    }


    private InitResult<S> mapResult(Object[] result) {
        return new InitResult<>(result[1], ListUtil.sleListToList((Object[]) result[2]));
    }
}
