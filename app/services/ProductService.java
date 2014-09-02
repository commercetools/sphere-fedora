package services;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.LineItem;
import models.ShopLineItem;
import models.ShopProduct;
import play.libs.F;

import java.util.Locale;

public interface ProductService {

    /**
     * Fetches the product with the provided product ID and variant ID.
     * @param productId internal identifier of the product.
     * @param variantId identifier of the product variant.
     * @return the promise of the product with this ID and the selected variant with this ID, or absent if it does not exist.
     */
    F.Promise<Optional<ShopProduct>> fetchById(String productId, int variantId);

    /**
     * Fetches the product with the provided slug and variant ID.
     * @param locale the selected locale corresponding to the product slug.
     * @param productSlug external and human-readable identifier of the product.
     * @param variantId identifier of the product variant.
     * @return the promise of the product with this slug and the selected variant with this ID, or absent if it does not exist.
     */
    F.Promise<Optional<ShopProduct>> fetchBySlug(Locale locale, String productSlug, int variantId);

    /**
     * Fetches the product with the provided SKU.
     * @param sku external identifier of the product variant.
     * @return the promise of the product with the selected variant with this SKU, or absent if it does not exist.
     */
    F.Promise<Optional<ShopProduct>> fetchBySku(String sku);

    /**
     * Fetches the product related information of the provided line item.
     * @param lineItem the line item from which to fetch its product information.
     * @return the promise of the line item with the product information, or absent if it does not exist.
     */
    F.Promise<Optional<ShopLineItem>> fetchByLineItem(LineItem lineItem);
}
