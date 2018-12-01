package test;

import java.util.function.Function;
import static za.co.no9.sle.runtime.Builtin.*;

public class ListType {

    public static final int Nothing$ = 0;

    /**
     * Nothing: <0> Maybe '0
     */
    public static final Object Nothing = new Object[] { Nothing$ };

    public static final int Just$ = 1;

    /**
     * Just: <0> '0 -> Maybe '0
     */
    public static final Function<Object, Object> Just = new Function<Object, Object>() {

        public Object apply(Object v0) {
            return new Object[] { Just$, v0 };
        }
    };

    public static final int Nil$ = 0;

    /**
     * Nil: <0> List '0
     */
    public static final Object Nil = new Object[] { Nil$ };

    public static final int Cons$ = 1;

    /**
     * Cons: <0> '0 -> List '0 -> List '0
     */
    public static final Function<Object, Function<Object, Object>> Cons = new Function<Object, Function<Object, Object>>() {

        public Function<Object, Object> apply(Object v0) {
            return new Function<Object, Object>() {

                public Object apply(Object v1) {
                    return new Object[] { Cons$, v0, v1 };
                }
            };
        }
    };

    /**
     * singleton: <0> '0 -> List '0
     */
    public static final Function<Object, Object> singleton = new Function<Object, Object>() {

        public Object apply(Object x) {
            return Cons.apply(x).apply(Nil);
        }
    };

    /**
     * double: <0> '0 -> '0 -> List '0
     */
    public static final Function<Object, Function<Object, Object>> double = new Function<Object, Function<Object, Object>>() {

        public Function<Object, Object> apply(Object x) {
            return new Function<Object, Object>() {

                public Object apply(Object y) {
                    return Cons.apply(x).apply(singleton.apply(y));
                }
            };
        }
    };

    /**
     * singleIntList: <> List Data.Int
     */
    public static final Object singleIntList = singleton.apply(10);

    /**
     * doubleIntList: <> List Data.Int
     */
    public static final Object doubleIntList = double.apply(1).apply(2);

    /**
     * doubleBooleanList: <> List Data.Bool
     */
    public static final Object doubleBooleanList = double.apply(true).apply(false);

    /**
     * head: <0> List '0 -> Maybe '0
     */
    public static final Function<Object, Object> head = new Function<Object, Object>() {

        public Object apply(Object $v0) {
            return new java.util.function.Supplier<java.lang.Object>() {

                public java.lang.Object get() {
                    java.lang.Object[] $$v0 = (java.lang.Object[]) $v0;
                    switch((int) $$v0[0]) {
                        case Nil$:
                            return Nothing;
                        case Cons$:
                            {
                                java.lang.Object $$0 = $$v0[1], $$1 = $$v0[2];
                                return Just.apply($$0);
                            }
                        default:
                            throw new RuntimeException("No case expression: " + $$v0);
                    }
                }
            }.get();
        }
    };

    /**
     * tail: <0> List '0 -> Maybe List '0
     */
    public static final Function<Object, Object> tail = new Function<Object, Object>() {

        public Object apply(Object $v0) {
            return new java.util.function.Supplier<java.lang.Object>() {

                public java.lang.Object get() {
                    java.lang.Object[] $$v0 = (java.lang.Object[]) $v0;
                    switch((int) $$v0[0]) {
                        case Nil$:
                            return Nothing;
                        case Cons$:
                            {
                                java.lang.Object $$2 = $$v0[1], $$3 = $$v0[2];
                                return Just.apply($$3);
                            }
                        default:
                            throw new RuntimeException("No case expression: " + $$v0);
                    }
                }
            }.get();
        }
    };

    /**
     * map: <0, 1> ('0 -> '1) -> List '0 -> List '1
     */
    public static final Function<Function<Object, Object>, Function<Object, Object>> map = new Function<Function<Object, Object>, Function<Object, Object>>() {

        public Function<Object, Object> apply(Function<Object, Object> $v0) {
            return new Function<Object, Object>() {

                public Object apply(Object $v1) {
                    return new java.util.function.Supplier<java.lang.Object>() {

                        public java.lang.Object get() {
                            java.lang.Object[] $$v1 = (java.lang.Object[]) $v1;
                            switch((int) $$v1[0]) {
                                case Nil$:
                                    return Nil;
                                case Cons$:
                                    {
                                        java.lang.Object $$4 = $$v1[1], $$5 = $$v1[2];
                                        return Cons.apply($v0.apply($$4)).apply(map.apply($v0).apply($$5));
                                    }
                                default:
                                    throw new RuntimeException("No case expression: " + $$v1);
                            }
                        }
                    }.get();
                }
            };
        }
    };
}
