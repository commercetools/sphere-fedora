package services;

import com.google.common.base.Optional;
import io.sphere.client.ProductSort;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static utils.AsyncUtils.asPromise;

@Singleton
public class ProductServiceImpl implements ProductService {
    protected static final int RECOMMENDED_PRODUCTS_SIZE = 20;
    protected static final int NEW_PRODUCTS_SIZE = 20;
    protected static final int OFFERS_PRODUCTS_SIZE = 20;

    private final Sphere sphere;

    @Inject
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
        SearchRequest<Product> searchRequest = sphere.products().filter(locale, filterByCategory(category))
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
    public F.Promise<ProductList> fetchNewProducts() {
        SearchRequest<Product> searchRequest = sphere.products().all().sort(ProductSort.price.desc)
                .page(0).pageSize(NEW_PRODUCTS_SIZE);
        return searchRequest.fetchAsync()
                .map(new F.Function<SearchResult<Product>, ProductList>() {
                    @Override
                    public ProductList apply(SearchResult<Product> result) throws Throwable {
                        return ProductList.of(result);
                    }
                });
    }

    @Override
    public F.Promise<ProductList> fetchProductsInOffer() {
        SearchRequest<Product> searchRequest = sphere.products().all().sort(ProductSort.price.asc)
                .page(0).pageSize(OFFERS_PRODUCTS_SIZE);
        return searchRequest.fetchAsync()
                .map(new F.Function<SearchResult<Product>, ProductList>() {
                    @Override
                    public ProductList apply(SearchResult<Product> result) throws Throwable {
                        return ProductList.of(result);
                    }
                });
    }

    @Override
    public F.Promise<Optional<ProductList>> fetchRecommendedProducts(final Locale locale, final ShopProduct product) {
        List<ShopCategory> categories = product.getCategories();
        if (categories.isEmpty()) {
            return asPromise(Optional.<ProductList>absent());
        } else {
            SearchRequest<Product> searchRequest = sphere.products().filter(locale, filterByCategories(categories))
                    .page(0).pageSize(RECOMMENDED_PRODUCTS_SIZE);
            return searchRequest.fetchAsync()
                    .map(new F.Function<SearchResult<Product>, Optional<ProductList>>() {
                        @Override
                        public Optional<ProductList> apply(SearchResult<Product> result) throws Throwable {
                            ProductList productList = ProductList.of(result);
                            return Optional.of(productList);
                        }
                    });
        }
    }

    protected static FilterExpression filterBySku(String sku) {
        List<String> skuList = Arrays.asList(sku);
        return new FilterExpressions.StringAttribute.EqualsAnyOf("variants.sku", skuList);
    }

    protected static FilterExpression filterByCategories(List<ShopCategory> shopCategories) {
        List<Category> categories = new ArrayList<Category>();
        for (ShopCategory shopCategory : shopCategories) {
            categories.add(shopCategory.get());
        }
        return new FilterExpressions.CategoriesOrSubcategories(categories);
    }

    protected static FilterExpression filterByCategory(ShopCategory category) {
        List<Category> categories = Arrays.asList(category.get());
        return new FilterExpressions.CategoriesOrSubcategories(categories);
    }
}
