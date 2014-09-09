package services;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.ShippingMethod;
import play.libs.F;

import java.util.List;

public interface ShippingMethodService {
    F.Promise<List<ShippingMethod>> getShippingMethods();

    F.Promise<Optional<ShippingMethod>> fetchById(String shippingMethodId);
}
