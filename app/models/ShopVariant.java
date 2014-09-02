package models;

import io.sphere.client.shop.model.Attribute;
import io.sphere.client.shop.model.Image;
import io.sphere.client.shop.model.Variant;

import java.util.ArrayList;
import java.util.List;

public class ShopVariant {
    private final Variant variant;

    ShopVariant(Variant variant) {
        this.variant = variant;
    }

    public int getId() {
        return variant.getId();
    }

    public String getSku() {
        return variant.getSKU();
    }

    public List<Attribute> getAttributes(final List<String> attributeNames) {
        List<Attribute> attributes = new ArrayList<>();
        for (String attributeName : attributeNames) {
            attributes.add(variant.getAttribute(attributeName));
        }
        return attributes;
    }

    public List<Image> getImages() {
        return variant.getImages();
    }

    @Override
    public String toString() {
        return "ShopVariant{" +
                "variant=" + variant +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopVariant variant1 = (ShopVariant) o;

        if (variant != null ? !variant.equals(variant1.variant) : variant1.variant != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return variant != null ? variant.hashCode() : 0;
    }
}
