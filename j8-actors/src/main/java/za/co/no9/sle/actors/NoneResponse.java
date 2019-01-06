package za.co.no9.sle.actors;


public class NoneResponse<S> extends Response<S> {
    public static final NoneResponse INSTANCE =
            new NoneResponse<>();


    @Override
    S process(S def) {
        return def;
    }
}
