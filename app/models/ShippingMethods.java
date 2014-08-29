package models;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.ShippingMethod;

public class ShippingMethods {
    private final List<ShippingMethod> methods;

    ShippingMethods(List<ShippingMethod> methods) {
        this.methods = methods;
    }

    public static ShippingMethods of(Optional<List<ShippingMethod>> shippingMethods) {
        return new ShippingMethods(shippingMethods.or(Collections.<ShippingMethod>emptyList()));
    }

    public static ShippingMethods empty() {
        return new ShippingMethods(Collections.<ShippingMethod>emptyList());
    }

    public List<ShippingMethod> all() {
        return methods;
    }

    @Override
    public String toString() {
        return "ShippingMethods{" +
            "methods=" + methods +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShippingMethods that = (ShippingMethods) o;

        if (methods != null ? !methods.equals(that.methods) : that.methods != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return methods != null ? methods.hashCode() : 0;
    }
}
