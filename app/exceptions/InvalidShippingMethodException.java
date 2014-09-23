package exceptions;

import static java.lang.String.format;

public class InvalidShippingMethodException extends RuntimeException {
    private InvalidShippingMethodException(final String message) {
        super(message);
    }

    public static InvalidShippingMethodException ofWrongId(final String id) {
        return new InvalidShippingMethodException(format("A shipping method with ID=%s does not exist.", id));
    }
}
