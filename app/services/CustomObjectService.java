package services;

import com.google.common.base.Optional;
import io.sphere.client.model.CustomObject;
import play.libs.F;

public interface CustomObjectService {

    /**
     * Fetches a custom object.
     * @param container to identify the custom object.
     * @param key to identify the custom object.
     * @return the promise of the custom object.
     */
    F.Promise<Optional<CustomObject>> getCustomObject(String container, String key);

    /**
     * Updates a custom object with the provided information.
     * @param container to identify the custom object.
     * @param key to identify the custom object.
     * @param data the information to set to the custom object.
     * @return the promise of the updated custom object that contains the provided information.
     */
    <T> F.Promise<CustomObject> setCustomObject(String container, String key, T data, Optional<Integer>version);
}
