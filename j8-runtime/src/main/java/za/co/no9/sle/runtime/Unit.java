package za.co.no9.sle.runtime;

public class Unit {
    public static final Unit INSTANCE =
            new Unit();


    private Unit() {
    }


    @Override
    public String toString() {
        return "Unit";
    }


    @Override
    public int hashCode() {
        return 0;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Unit;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this;
    }
}
