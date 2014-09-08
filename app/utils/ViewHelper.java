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

	/**
	 * Returns the current Cart in session.
	 */
	public static Cart getCurrentCart() {
		return Sphere.getInstance().currentCart().fetch();
	}

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

	/**
	 * Returns the list of root categories
	 */
	public static List<Category> getRootCategories() {
        return Sphere.getInstance().categories().getRoots();
	}


    public static String getReturnUrl() {
        return current().session().get("returnUrl");
    }

    public static List<Lang> getLanguages() {
        return Lang.availables();
    }

    public static String capitalizeInitials(String text) {
        return WordUtils.capitalizeFully(text);
    }

    public static String abbreviate(String text, int maxWidth) {
        if (text == null) return "";
        return StringUtils.abbreviate(text, maxWidth);
    }

    public static BigDecimal getPercentage(double amount) {
        return BigDecimal.valueOf(amount * 100).stripTrailingZeros();
    }

    public static boolean isSet(Object object) {
        return object != null;
    }

    /**
	 * Check whether the given product has more than one attribute value
	 * 
	 * @param product
     * @param attributeName
	 * @return true if the product has more than one attribute value, false otherwise
	 */
	public static boolean hasMoreAttributeValues(Product product, String attributeName) {
        return product.getVariants().getAvailableAttributes(attributeName).size() > 1;
    }

    public static Money getShippingCost() {
        // TODO Implement correct shipping cost
        return new Money(BigDecimal.valueOf(10), "EUR");
    }

    public static Map<Lang, String> getLocalizedUrls() {
        Map<Lang, String> localizedUrls = new HashMap<Lang, String>();
        for (Lang language : getLanguages()) {
            String url = "";
            if (!language.equals(lang())) url = buildQuery("lang", language.language());
            localizedUrls.put(language, url);
        }
        return localizedUrls;
    }

    public static String displayAttributeId(Attribute attribute) {
        if (isEnumAttribute(attribute)) {
            return attribute.getEnum().key;
        } else if (isNumberAttribute(attribute)) {
            return String.valueOf(attribute.getInt());
        } else {
            return attribute.getString();
        }
    }

    public static String displayAttributeValue(Attribute attribute, Locale locale) {
        if (attribute == null) {
            return "";
        } else if (isNumberAttribute(attribute)) {
            return String.valueOf(attribute.getInt());
        } else if (isEnumAttribute(attribute)) {
            if (isLocalizedEnumAttribute(attribute)) {
                return attribute.getLocalizableEnum().getLabel().get(locale);
            } else {
                return attribute.getEnum().label;
            }
        } else {
            String localizedString = getLocalizedString(attribute).get(locale);
            if (localizedString.isEmpty()) {
                return attribute.getString();
            } else {
                return localizedString;
            }
        }
    }

    public static LocalizedString getLocalizedString(Attribute attribute) {
        LocalizedString result = Attribute.defaultLocalizedString;
        if (attribute.getValue() instanceof Map) {
            final Map data = (Map) attribute.getValue();
            result = extractLocalizedString(data);
        }
        return result;
    }

    @SuppressWarnings("unchecked")//since object has no type information it needs to be casted
    private static LocalizedString extractLocalizedString(Map data) {
        final Map<Locale, String> stringMap = Maps.newHashMap();
        Map<String, String> localeMap = (Map<String, String>)data;
        for (Map.Entry<String, String> entry : localeMap.entrySet()) {
            stringMap.put(Util.fromLanguageTag(entry.getKey()), entry.getValue());
        }
        return new LocalizedString(stringMap);
    }

    public static boolean isEnumAttribute(Attribute attribute) {
        String key = attribute.getEnum().key;
        return key != null && !key.isEmpty();
    }

    public static boolean isLocalizedEnumAttribute(Attribute attribute) {
        return !attribute.getLocalizableEnum().getKey().isEmpty();
    }

    public static boolean isNumberAttribute(Attribute attribute) {
        return attribute.getValue().getClass().equals(java.lang.Integer.class);
    }

    public static String buildQuery(String key, String value) {
        String queryString = "?";
        if (!value.isEmpty()) {
            queryString += key + "=" + value;
        }
        for (Map.Entry<String,String[]> entry : current().request().queryString().entrySet()) {
            if (entry.getKey().equals(key)) continue;
            for (String val : entry.getValue()) {
                queryString += "&" + entry.getKey() + "=" + val;
            }
        }
        return queryString;
    }
}