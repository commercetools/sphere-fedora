package exceptions;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(Throwable cause) {
        super(cause);
    }
}
