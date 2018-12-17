package test;


public class ListType {
    public static final int Nil$ = 0;

    /**
     * Nil: <0> file.package.name.File.List '0
     */
    public static final java.lang.Object Nil = new java.lang.Object[] { Nil$ };

    public static final int Cons$ = 1;

    /**
     * Cons: <0> '0 -> file.package.name.File.List '0 -> file.package.name.File.List '0
     */
    public static final java.lang.Object Cons = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object v0) {
            return new java.util.function.Function<java.lang.Object, java.lang.Object>() {
                    public java.lang.Object apply(java.lang.Object v1) {
                        return new java.lang.Object[] { Cons$, v0, v1 };
                    }
                };
        }
    };

    /**
     * singleton: <0> '0 -> file.package.name.File.List '0
     */
    public static final java.lang.Object singleton = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object x) {
            return ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) Cons).apply(x)).apply(Nil);
        }
    };

    /**
     * double: <0> '0 -> '0 -> file.package.name.File.List '0
     */
    public static final java.lang.Object double = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object x) {
            return new java.util.function.Function<java.lang.Object, java.lang.Object>() {
                    public java.lang.Object apply(java.lang.Object y) {
                        return ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) Cons).apply(x)).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) singleton).apply(y));
                    }
                };
        }
    };

    /**
     * singleIntList: <> file.package.name.File.List Data.Int
     */
    public static final java.lang.Object singleIntList = ((java.util.function.Function<java.lang.Object, java.lang.Object>) singleton).apply(10);

    /**
     * doubleIntList: <> file.package.name.File.List Data.Int
     */
    public static final java.lang.Object doubleIntList = ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) double).apply(1)).apply(2);

    /**
     * doubleBooleanList: <> file.package.name.File.List Data.Bool
     */
    public static final java.lang.Object doubleBooleanList = ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) double).apply(true)).apply(false);

    /**
     * head: <0> file.package.name.File.List '0 -> /home/alfred/.sle/Maybe '0
     */
    public static final java.lang.Object head = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object $v0) {
            return new java.util.function.Supplier<java.lang.Object>() {
                    public java.lang.Object get() {
                        java.lang.Object[] $$v0 = ((java.lang.Object[]) $v0);
                        switch(((int) $$v0[0])) {
                            case Nil$:
                                return file.package.name.File.Nothing;
                            case Cons$: {
                                java.lang.Object $$0 = $$v0[1];
                                java.lang.Object $$1 = $$v0[2];
                                return ((java.util.function.Function<java.lang.Object, java.lang.Object>) file.package.name.File.Just).apply($$0);
                            }
                            default:
                                throw new java.lang.RuntimeException(("No case expression: " + $$v0));
                        }
                    }
                }.get();
        }
    };

    /**
     * tail: <0> file.package.name.File.List '0 -> /home/alfred/.sle/Maybe file.package.name.File.List '0
     */
    public static final java.lang.Object tail = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object $v0) {
            return new java.util.function.Supplier<java.lang.Object>() {
                    public java.lang.Object get() {
                        java.lang.Object[] $$v0 = ((java.lang.Object[]) $v0);
                        switch(((int) $$v0[0])) {
                            case Nil$:
                                return file.package.name.File.Nothing;
                            case Cons$: {
                                java.lang.Object $$2 = $$v0[1];
                                java.lang.Object $$3 = $$v0[2];
                                return ((java.util.function.Function<java.lang.Object, java.lang.Object>) file.package.name.File.Just).apply($$3);
                            }
                            default:
                                throw new java.lang.RuntimeException(("No case expression: " + $$v0));
                        }
                    }
                }.get();
        }
    };

    /**
     * map: <0, 1> ('0 -> '1) -> file.package.name.File.List '0 -> file.package.name.File.List '1
     */
    public static final java.lang.Object map = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object $v0) {
            return new java.util.function.Function<java.lang.Object, java.lang.Object>() {
                    public java.lang.Object apply(java.lang.Object $v1) {
                        return new java.util.function.Supplier<java.lang.Object>() {
                    public java.lang.Object get() {
                        java.lang.Object[] $$v1 = ((java.lang.Object[]) $v1);
                        switch(((int) $$v1[0])) {
                            case Nil$:
                                return Nil;
                            case Cons$: {
                                java.lang.Object $$4 = $$v1[1];
                                java.lang.Object $$5 = $$v1[2];
                                return ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) Cons).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) $v0).apply($$4))).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) map).apply($v0)).apply($$5));
                            }
                            default:
                                throw new java.lang.RuntimeException(("No case expression: " + $$v1));
                        }
                    }
                }.get();
                    }
                };
        }
    };
}