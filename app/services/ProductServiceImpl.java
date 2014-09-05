package services;

import com.google.common.base.Optional;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.filters.expressions.FilterExpressions;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.LineItem;
import io.sphere.client.shop.model.Product;
import models.*;
import play.libs.F;
import sphere.SearchRequest;
import sphere.Sphere;

import java.util.*;

public class ProductServiceImpl implements ProductService {
    private final Sphere sphere;

    public ProductServiceImpl(final Sphere sphere) {
        this.sphere = sphere;
    }

    @Override
    public F.Promise<Optional<ShopProduct>> fetchById(final String productId, final int variantId) {
        return sphere.products().byId(productId).fetchAsync()
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
    public F.Promise<Optional<ShopProduct>> fetchBySlug(final Locale locale, final String productSlug, final int variantId) {
        return sphere.products().bySlug(locale, productSlug).fetchAsync()
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
        FilterExpression skuFilter = filterBySku(sku);
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

    @Override
    public F.Promise<ProductList> fetchSearchedProducts(final Locale locale, int page, final RequestParameters parameters) {
        SearchRequest<Product> searchRequest = sphere.products().filter(locale, parameters.getFilters())
                .facet(parameters.getFacets()).sort(parameters.getSort()).page(page).pageSize(parameters.getPageSize());
        return searchRequest.fetchAsync()
                .map(new F.Function<SearchResult<Product>, ProductList>() {
                @Override
                public ProductList apply(SearchResult<Product> result) throws Throwable {
                    return ProductList.of(result, parameters);
                }
            });
    }

    @Override
    public F.Promise<ProductList> fetchCategoryProducts(final Locale locale, final ShopCategory category, int page,
                                                        final RequestParameters parameters) {
        SearchRequest<Product> searchRequest = sphere.products().filter(filterByCategory(category))
                .facet(parameters.getFacets()).sort(parameters.getSort()).page(page).pageSize(parameters.getPageSize());
        return searchRequest.fetchAsync()
                .map(new F.Function<SearchResult<Product>, ProductList>() {
                    @Override
                    public ProductList apply(SearchResult<Product> result) throws Throwable {
                        return ProductList.of(result, parameters);
                    }
                });
    }

    protected static FilterExpression filterBySku(String sku) {
        List<String> skuList = Arrays.asList(sku);
        return new FilterExpressions.StringAttribute.EqualsAnyOf("variants.sku", skuList);
    }

    protected static FilterExpression filterByCategory(ShopCategory category) {
        List<Category> categories = Arrays.asList(category.get());
        return new FilterExpressions.CategoriesOrSubcategories(categories);
    }
}
