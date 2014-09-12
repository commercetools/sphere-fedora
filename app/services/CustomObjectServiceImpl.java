package services;

import com.google.common.base.Optional;
import io.sphere.client.CommandRequest;
import io.sphere.client.SphereResult;
import io.sphere.client.exceptions.SphereBackendException;
import io.sphere.client.model.CustomObject;
import play.Logger;
import play.libs.F;
import sphere.Sphere;
import sphere.util.Async;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CustomObjectServiceImpl implements CustomObjectService {
    protected final Sphere sphere;

    @Inject
    public CustomObjectServiceImpl(final Sphere sphere) {
        this.sphere = sphere;
    }

    public F.Promise<Optional<CustomObject>> getCustomObject(final String container, final String key) {
        return sphere.customObjects().get(container, key).fetchAsync();
    }

    public <T> F.Promise<CustomObject> setCustomObject(final String container, final String key, final T data,
                                                       final Optional<Integer> version) {
        CommandRequest<CustomObject> request;
        if (version.isPresent()) {
            request = sphere.client().customObjects().set(container, key, data, version.get());
        } else {
            request = sphere.client().customObjects().set(container, key, data);
        }
        return Async.asPlayPromise(request.executeAsync()).map(new F.Function<SphereResult<CustomObject>, CustomObject>() {
            @Override
            public CustomObject apply(SphereResult<CustomObject> sphereResult) throws Throwable {
                if (sphereResult.isSuccess()) {
                    return sphereResult.getValue();
                } else {
                    // TODO Handle concurrent modification
                    SphereBackendException exception = sphereResult.getGenericError();
                    Logger.error(String.format("Custom object %s %s could not be updated with %s: %s",
                            container, key, data.toString(), exception.getMessage()));
                    throw exception;
                }
            }
        });
    }
}
