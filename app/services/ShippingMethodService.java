package services;

import io.sphere.client.shop.model.ShippingMethod;
import play.libs.F;

import java.util.List;

public interface ShippingMethodService {
    F.Promise<List<ShippingMethod>> getShippingMethods();
}
