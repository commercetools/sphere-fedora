package utils;

import java.math.BigDecimal;
import java.util.*;

import com.google.common.collect.Maps;
import com.neovisionaries.i18n.CountryCode;
import controllers.routes;
import io.sphere.client.model.LocalizedString;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.*;
import io.sphere.internal.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import play.i18n.Lang;
import sphere.Sphere;

import static play.mvc.Http.Context.Implicit.lang;
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