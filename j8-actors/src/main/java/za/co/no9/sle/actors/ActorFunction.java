package za.co.no9.sle.actors;


public interface ActorFunction<S, M> {
    InitResult<S> init(ActorRef<S, M> self);

    Response<S> update(S state, M message);
}
