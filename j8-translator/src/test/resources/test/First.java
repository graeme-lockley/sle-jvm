package test;


public class First {
    /**
     * factorial: <> Data.Int -> Data.Int
     */
    public static final java.lang.Object factorial = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object n) {
            return ((java.lang.Boolean) ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.EQUAL_EQUAL).apply(n)).apply(0)) ? 1 : ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.STAR).apply(n)).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) factorial).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.MINUS).apply(n)).apply(1)));
        }
    };

    /**
     * factorial2: <> Data.Int -> Data.Int
     */
    public static final java.lang.Object factorial2 = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object n) {
            return ((java.lang.Boolean) ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.EQUAL_EQUAL).apply(n)).apply(0)) ? 1 : ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.STAR).apply(n)).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) factorial2).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.MINUS).apply(n)).apply(1)));
        }
    };

    /**
     * factorial3: <> Data.Int -> Data.Int
     */
    public static final java.lang.Object factorial3 = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object $v0) {
            return ((java.lang.Boolean) ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.EQUAL_EQUAL).apply(0)).apply($v0)) ? 1 : ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.STAR).apply($v0)).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) factorial3).apply(((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.MINUS).apply($v0)).apply(1)));
        }
    };
}