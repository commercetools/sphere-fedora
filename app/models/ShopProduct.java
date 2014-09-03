package models;

import com.google.common.base.Optional;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopProduct {
    private final Product product;
    private final List<ShopVariant> variants;
    private final ShopVariant selectedVariant;

    ShopProduct(Product product, List<ShopVariant> variants, ShopVariant selectedVariant) {
        this.product = product;
        this.variants = variants;
        this.selectedVariant = selectedVariant;
    }

    public static ShopProduct of(Product product, int masterVariantId) {
        List<ShopVariant> variants = new ArrayList<>();
        for (Variant variant : product.getVariants()) {
            variants.add(new ShopVariant(variant, product.getTaxCategory()));
        }
        Variant masterVariant = product.getVariants().byId(masterVariantId).or(product.getMasterVariant());
        return new ShopProduct(product, variants, new ShopVariant(masterVariant, product.getTaxCategory()));
    }

    public static ShopProduct of(Product product) {
        return of(product, product.getMasterVariant().getId());
    }

    public static ShopProduct of(Product product, String masterVariantSku) {
        List<ShopVariant> variants = new ArrayList<>();
        for (Variant variant : product.getVariants()) {
            variants.add(new ShopVariant(variant, product.getTaxCategory()));
        }
        Variant masterVariant = product.getVariants().bySKU(masterVariantSku).or(product.getMasterVariant());
        return new ShopProduct(product, variants, new ShopVariant(masterVariant, product.getTaxCategory()));
    }

    public Product get() {
        return product;
    }

    public String getId() {
        return this.product.getId();
    }

    public VersionedId getVersionedId() {
        return this.product.getIdAndVersion();
    }

    public String getName(Locale locale) {
        return this.product.getName(locale);
    }

    public String getDescription(Locale locale) {
        return this.product.getDescription(locale);
    }

    public String getSlug(Locale locale) {
        return this.product.getSlug(locale);
    }

    public String getMetaTitle(Locale locale) {
        return this.product.getMetaTitle(locale);
    }

    public String getMetaDescription(Locale locale) {
        return this.product.getMetaDescription(locale);
    }

    public List<ShopVariant> getVariants() {
        return variants;
    }

    public ShopVariant getSelectedVariant() {
        return selectedVariant;
    }

    public Optional<ShopVariant> getVariantById(int variantId) {
        for (ShopVariant variant : getVariants()) {
            if (variant.getId() == variantId) {
                return Optional.of(variant);
            }
        }
        return Optional.absent();
    }

    /**
     * Gets all selectable variants for the selected variant and attribute.
     * @param selectedAttribute the selected attribute.
     * @param allSelectableAttributes the list of all selectable attributes of the shop.
     * @return a list of variants that represent the selected variant in different selected attribute values.
     */
    public List<ShopVariant> getSelectableVariants(String selectedAttribute, List<String> allSelectableAttributes) {
        List<String> selectableAttributes = new ArrayList<>(allSelectableAttributes);
        selectableAttributes.remove(selectedAttribute);
        VariantList matchingVariants = getMatchingVariantsForAttributes(product.getVariants(), selectableAttributes);
        VariantList selectableVariants = getDistinctVariantsForAttribute(matchingVariants, selectedAttribute);
        return convertToList(selectableVariants);
    }

    /**
     * Selects those variants from the list with the same attribute values (of the provided list) than the selected variant.
     * @param variantList the list of variants from which to select the variants.
     * @param attributeNames the attribute names from which to match the values.
     * @return the list of variants with the same selected attribute values.
     */
    private VariantList getMatchingVariantsForAttributes(final VariantList variantList, final List<String> attributeNames) {
        List<Attribute> matchingAttributeValues = selectedVariant.getAttributes(attributeNames);
        return variantList.byAttributes(matchingAttributeValues);
    }

    /**
     * For each distinct value of the provided attribute, selects one variant of the list with this value, if any.
     * @param variantList the list of variants from which to select the variants.
     * @param attributeName the attribute name from which to obtain the distinct values.
     * @return the list of variants where all possible values of the provided attribute are uniquely represented,
     * or missing if no variant had that particular attribute value.
     */
    private VariantList getDistinctVariantsForAttribute(final VariantList variantList, final String attributeName) {
        List<Attribute> distinctAttributeValues = product.getVariants().getAvailableAttributes(attributeName);
        List<Variant> selectableVariants = new ArrayList<>();
        for (Attribute distinctAttributeValue : distinctAttributeValues) {
            Optional<Variant> selectableVariant = variantList.byAttributes(distinctAttributeValue).first();
            if (selectableVariant.isPresent()) {
                selectableVariants.add(selectableVariant.get());
            }
        }
        return new VariantList(selectableVariants);
    }

    /**
     * Converts a {@link io.sphere.client.shop.model.VariantList} to a list of shop variants.
     * @param variantList the list of variants to be converted.
     * @return the converted list of shop variants.
     */
    private List<ShopVariant> convertToList(final VariantList variantList) {
        List<ShopVariant> shopVariants =  new ArrayList<>();
        for (Variant variant : variantList) {
            shopVariants.add(new ShopVariant(variant, product.getTaxCategory()));
        }
        return shopVariants;
    }

    @Override
    public String toString() {
        return "ShopProduct{" +
                "product=" + product +
                ", variants=" + variants +
                ", selectedVariant=" + selectedVariant +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopProduct that = (ShopProduct) o;

        VersionedId versionedId = product.getIdAndVersion();
        VersionedId thatVersionedId = that.get().getIdAndVersion();

        if (!versionedId.getId().equals(thatVersionedId.getId())) return false;
        if (versionedId.getVersion() != thatVersionedId.getVersion()) return false;

        if (selectedVariant != null ? !selectedVariant.equals(that.selectedVariant) : that.selectedVariant != null)
            return false;
        if (variants != null ? !variants.equals(that.variants) : that.variants != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = product.getId().hashCode();
        result = 31 * result + (variants != null ? variants.hashCode() : 0);
        result = 31 * result + (selectedVariant != null ? selectedVariant.hashCode() : 0);
        return result;
    }
}
