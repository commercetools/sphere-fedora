package services;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.LineItem;
import models.ShopLineItem;
import models.ShopProduct;
import play.libs.F;

public interface ProductService {

    /**
     * Fetches the product with the provided slug and variant ID.
     * @param productSlug external and human-readable identifier of the product.
     * @param variantId identifier of the product variant.
     * @return the promise of the product with this slug and the selected variant with this ID, or absent if it does not exist.
     */
    F.Promise<Optional<ShopProduct>> fetchBySlug(String productSlug, int variantId);

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
