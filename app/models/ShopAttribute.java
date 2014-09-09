package models;

import io.sphere.client.model.LocalizedString;
import io.sphere.client.shop.model.Attribute;

import java.util.Locale;

public class ShopAttribute {
    private final Attribute attribute;

    ShopAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Attribute get() {
        return attribute;
    }

    public String getName() {
        return attribute.getName();
    }

    public boolean isEnumAttribute() {
        String key = attribute.getEnum().key;
        return key != null && !key.isEmpty();
    }

    public boolean isLocalizedEnumAttribute() {
        return !attribute.getLocalizableEnum().getKey().isEmpty();
    }

    public boolean isNumberAttribute() {
        return attribute.getValue() instanceof  java.lang.Integer;
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
