package test;


public class UnitValue {
    /**
     * calculate: <> Data.Unit -> Data.Int
     */
    public static final java.lang.Object calculate = new java.util.function.Function<java.lang.Object, java.lang.Object>() {
        public java.lang.Object apply(java.lang.Object n) {
            return 100;
        }
    };

    /**
     * value: <> Data.Int
     */
    public static final java.lang.Object value = ((java.util.function.Function<java.lang.Object, java.lang.Object>) calculate).apply(za.co.no9.sle.runtime.Unit.INSTANCE);
}