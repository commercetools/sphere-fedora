package utils;

import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.*;
import sphere.Sphere;

import static play.mvc.Http.Context.current;

public class ViewHelper {

    public static boolean isLoggedIn() {
        return Sphere.getInstance().isLoggedIn();
    }

    public static Address getShippingAddress(Cart cart) {
        if (cart.getShippingAddress() != null) {
            return cart.getShippingAddress();
        }
        Address address = new Address(CountryCode.DE);
        if (Sphere.getInstance().isLoggedIn()) {
            Customer customer = Sphere.getInstance().currentCustomer().fetch();
            address.setEmail(customer.getEmail());
            address.setFirstName(customer.getName().getFirstName());
            address.setLastName(customer.getName().getLastName());
            address.setTitle(customer.getName().getTitle());
        }
        return address;
    }

    public static Address getBillingAddress(Cart cart) {
        if (cart.getBillingAddress() != null) {
            return cart.getBillingAddress();
        }
        if (cart.getShippingAddress() != null) {
            return cart.getShippingAddress();
        }
        return new Address(CountryCode.DE);
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