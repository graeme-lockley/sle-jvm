package test;


public class BinaryOperator {
    /**
     * (++): <> Data.Int -> Data.Int -> Data.Int
     */
    public static final java.lang.Object $2b$2b = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object a) {
            return new java.util.function.Function<java.lang.Object, java.lang.Object>() {
                    public java.lang.Object apply(java.lang.Object b) {
                        return ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) $2b).apply(a)).apply(b);
                    }
                };
        }
    };

    /**
     * value: <> Data.Int
     */
    public static final java.lang.Object value = ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) $2b$2b).apply(10)).apply(100);
}