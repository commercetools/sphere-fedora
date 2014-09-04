package services;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.LineItem;
import models.*;
import play.libs.F;

import java.util.Locale;
import java.util.Map;

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

    /**
     * Searches products matching the search input text and the facets specified in the query string of the request.
     * @param locale the selected locale corresponding to the search request.
     * @param parameters the parameters associated with the request.
     * @return the promise of the product list matching the request.
     */
    F.Promise<ProductList> fetchSearchedProducts(Locale locale, RequestParameters parameters);

    /**
     * Searches products belonging to the provided category and matching the facets specified in the query string of the request.
     * @param locale the selected locale corresponding to the search request.
     * @param category the requested category.
     * @param parameters the parameters associated with the request.
     * @return the promise of the product list matching the request.
     */
    F.Promise<ProductList> fetchCategoryProducts(Locale locale, ShopCategory category, RequestParameters parameters);
}
