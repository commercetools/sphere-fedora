package utils;

import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.SimpleResult;

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

    public static <T> F.Promise<T> asPromise(final T thing) {
        return F.Promise.<T>pure(thing);
    }

    public static F.Promise<Result> asPromise(final Results.Status status) {
        return F.Promise.<Result>pure(status);
    }

    public static F.Promise<Result> asPromise(final SimpleResult simpleResult) {
        return F.Promise.<Result>pure(simpleResult);
    }
}
