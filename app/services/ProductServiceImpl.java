package services;

import com.google.common.base.Optional;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.filters.expressions.FilterExpressions;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.LineItem;
import io.sphere.client.shop.model.Product;
import models.ShopLineItem;
import models.ShopProduct;
import play.libs.F;
import sphere.Sphere;

import java.util.Arrays;
import java.util.List;

public class ProductServiceImpl implements ProductService {
    public static final String FILTER_ATTRIBUTE_SKU = "variants.sku";

    private final Sphere sphere;

    public ProductServiceImpl(final Sphere sphere) {
        this.sphere = sphere;
    }

    @Override
    public F.Promise<Optional<ShopProduct>> fetchBySlug(final String productSlug, final int variantId) {
        return sphere.products().bySlug(productSlug).fetchAsync()
                .map(new F.Function<Optional<Product>, Optional<ShopProduct>>() {
                    @Override
                    public Optional<ShopProduct> apply(Optional<Product> product) throws Throwable {
                        if (product.isPresent()) {
                            ShopProduct fetchedProduct = ShopProduct.of(product.get(), variantId);
                            return Optional.of(fetchedProduct);
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }

    @Override
    public F.Promise<Optional<ShopProduct>> fetchBySku(final String sku) {
        List<String> skuList = Arrays.asList(sku);
        FilterExpression skuFilter = new FilterExpressions.StringAttribute.EqualsAnyOf(FILTER_ATTRIBUTE_SKU, skuList);
        return sphere.products().filter(skuFilter).fetchAsync()
                .map(new F.Function<SearchResult<Product>, Optional<ShopProduct>>() {
                    @Override
                    public Optional<ShopProduct> apply(SearchResult<Product> result) throws Throwable {
                        if (result.getResults().isEmpty()) {
                            return Optional.absent();
                        } else {
                            ShopProduct fetchedProduct = ShopProduct.of(result.getResults().get(0), sku);
                            return Optional.of(fetchedProduct);
                        }
                    }
                });
    }

    @Override
    public F.Promise<Optional<ShopLineItem>> fetchByLineItem(final LineItem lineItem) {
        return sphere.products().byId(lineItem.getProductId()).fetchAsync()
                .map(new F.Function<Optional<Product>, Optional<ShopLineItem>>() {
                    @Override
                    public Optional<ShopLineItem> apply(Optional<Product> product) throws Throwable {
                        if (product.isPresent()) {
                            ShopProduct fetchedProduct = ShopProduct.of(product.get(), lineItem.getVariant().getId());
                            ShopLineItem fetchedLineItem = ShopLineItem.of(lineItem, fetchedProduct);
                            return Optional.of(fetchedLineItem);
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }
}
