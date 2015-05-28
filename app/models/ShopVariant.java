package models;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.model.Money;
import io.sphere.client.model.Reference;
import io.sphere.client.shop.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static utils.PriceUtils.customerPrice;

public class ShopVariant {
    private final Variant variant;
    private final Reference<TaxCategory> taxCategory;

    ShopVariant(Variant variant, Reference<TaxCategory> taxCategory) {
        this.variant = variant;
        this.taxCategory = taxCategory;
    }

    public Variant get() {
        return variant;
    }

    public int getId() {
        return variant.getId();
    }

    public String getSku() {
        return variant.getSKU();
    }

    public Optional<Price> getPrice(UserContext context) {
        Price price;
        if (context.customer().isPresent()) {
            Reference<CustomerGroup> customerGroup = context.customer().get().getCustomerGroupReference();
            price = variant.getPrice(context.currency(), context.country(), customerGroup);
        } else {
            price = variant.getPrice(context.currency(), context.country());
        }
        return Optional.fromNullable(price);
    }

    public Optional<TaxRate> getTaxRate(CountryCode country) {
        if (taxCategory.isExpanded()) {
            for (TaxRate rate : taxCategory.get().getRates()) {
                if (country.equals(rate.getCountry())) {
                    return Optional.of(rate);
                }
            }
        }
        return Optional.absent();
    }

    public Optional<ShopAttribute> getAttribute(String attributeName) {
        Attribute attribute = variant.getAttribute(attributeName);
        if (attribute == null) {
            return Optional.absent();
        } else {
            ShopAttribute shopAttribute = ShopAttribute.of(attribute);
            return Optional.of(shopAttribute);
        }
    }

    public List<ShopAttribute> getAttributes() {
        List<ShopAttribute> attributes = new ArrayList<ShopAttribute>();
        for(Attribute attribute : variant.getAttributes()) {
            attributes.add(new ShopAttribute(attribute));
        }
        return attributes;
    }

    public List<Attribute> getAttributes(final List<String> attributeNames) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (String attributeName : attributeNames) {
            attributes.add(variant.getAttribute(attributeName));
        }
        return attributes;
    }

    public List<Image> getImages() {
        return variant.getImages();
    }

    public ScaledImage getMainImage(ImageSize imageSize) {
        return variant.getFeaturedImage().getSize(imageSize);
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

        ShopVariant that = (ShopVariant) o;

        if (variant.getId() != that.get().getId()) return false;
        if (!variant.getSKU().equals(that.get().getSKU())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return variant.getSKU().hashCode();
    }
}
