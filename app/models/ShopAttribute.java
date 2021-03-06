package models;

import io.sphere.client.model.LocalizedString;
import io.sphere.client.shop.model.Attribute;

import java.util.Locale;
import java.util.Map;

public class ShopAttribute {
    private final Attribute attribute;

    ShopAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public static ShopAttribute of(Attribute attribute) {
        return new ShopAttribute(attribute);
    }

    public Attribute get() {
        return attribute;
    }

    public String getName() {
        return attribute.getName();
    }

    public boolean isEnumAttribute() {
        Object value = attribute.getValue();
        return value instanceof Map && ((Map) value).containsKey("label") && ((Map) value).containsKey("key");
    }

    public boolean isLocalizedEnumAttribute() {
        Object value = attribute.getValue();
        return isEnumAttribute() && value instanceof Map && ((Map) value).get("label") instanceof Map;
    }

    public boolean isNumberAttribute() {
        return attribute.getValue() instanceof java.lang.Integer;
    }

    public LocalizedString getLocalizedString() {
        return attribute.getLocalizedString();
    }

    public String getStringValue(Locale locale) {
        if (isNumberAttribute()) {
            return String.valueOf(attribute.getInt());
        } else if (isEnumAttribute()) {
            if (isLocalizedEnumAttribute()) {
                return attribute.getLocalizableEnum().getLabel().get(locale);
            } else {
                return attribute.getEnum().label;
            }
        } else {
            String localizedString = getLocalizedString().get(locale);
            if (localizedString.isEmpty()) {
                return attribute.getString();
            } else {
                return localizedString;
            }
        }
    }
}
