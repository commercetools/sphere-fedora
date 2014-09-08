package exceptions;

public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException(Throwable cause) {
        super(cause);
    }
}
