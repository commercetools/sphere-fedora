package utils;

public final class AsyncUtils {

    private AsyncUtils() {
    }

    /**
     * Default timeout in milliseconds for asynchronous calls.
     * In Java 7 handling asynchronous calls can be very verbose, thus in some cases it might help making code
     * more readable to use a defined timeout to avoid propagating promises.
     * @return the default timeout in milliseconds.
     */
    public static long defaultTimeout() {
        return 100000;
    }
}
