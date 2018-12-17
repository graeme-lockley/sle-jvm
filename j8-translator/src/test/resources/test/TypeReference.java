package test;


public class TypeReference {
    /**
     * equal: <0> '0 -> '0 -> Data.Bool
     */
    public static final java.lang.Object equal = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object a) {
            return new java.util.function.Function<java.lang.Object, java.lang.Object>() {
                    public java.lang.Object apply(java.lang.Object b) {
                        return ((java.util.function.Function<java.lang.Object, java.lang.Object>) ((java.util.function.Function<java.lang.Object, java.lang.Object>) za.co.no9.sle.runtime.Builtin.EQUAL_EQUAL).apply(a)).apply(b);
                    }
                };
        }
    };
}