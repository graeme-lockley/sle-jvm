package za.co.no9.sle.runtime;

public class IllegalBuiltInNameException extends RuntimeException {
    public IllegalBuiltInNameException(String name) {
        super(name);
    }

    public IllegalBuiltInNameException(String name, Exception e) {
        super(name, e);
    }
}
