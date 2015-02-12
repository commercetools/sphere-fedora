package utils;


import io.sphere.client.model.Money;
import io.sphere.client.shop.model.*;
import sphere.Sphere;

import static play.mvc.Http.Context.current;

public class ViewHelper {

    public static boolean isLoggedIn() {
        return Sphere.getInstance().isLoggedIn();
    }

    public static Money getTotalPrice(Cart cart) {
        if (cart.getShippingInfo() != null) {
            return cart.getTotalPrice().plus(cart.getShippingInfo().getPrice());
        }
        return cart.getTotalPrice();
    }

    public static String getReturnUrl() {
        return current().session().get("returnUrl");
    }

    public static boolean isSet(Object object) {
        return object != null;
    }
}