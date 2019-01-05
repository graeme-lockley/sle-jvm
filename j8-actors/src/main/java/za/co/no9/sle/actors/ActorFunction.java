package za.co.no9.sle.actors;


public interface ActorFunction<S, M> {
    UpdateResult<S> init(ActorRef<S, M> self);

    UpdateResult<S> update(S state, M message);
}
