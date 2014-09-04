package services;

import com.google.common.base.Optional;
import com.google.common.collect.Range;
import io.sphere.client.facets.Facet;
import io.sphere.client.facets.FacetParser;
import io.sphere.client.facets.Facets;
import io.sphere.client.facets.expressions.FacetExpression;
import io.sphere.client.filters.Filter;
import io.sphere.client.filters.FilterParser;
import io.sphere.client.filters.Filters;
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

import java.math.BigDecimal;
import java.util.*;

import static models.RequestParameters.*;

public class ProductServiceImpl implements ProductService {
    public static final Filters.Fulltext FILTER_SEARCH = filterRequestSearch();
    public static final Facets.MoneyAttribute.Ranges FILTER_PRICE = filterRequestPrice();

    protected static final String FILTER_ATTRIBUTE_SKU = "variants.sku";
    protected static final String FILTER_ATTRIBUTE_PRICE = "variants.price";

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
        FilterExpression skuFilter = filterExpressionOnlySku(sku);
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
    public F.Promise<ProductList> fetchSearchedProducts(final Locale locale, final Map<String, String[]> queryString,
                                                        final RequestParameters parameters) {
        // TODO try to use facets generically
        final List<FacetExpression> facets = FacetParser.parse(queryString, Arrays.<Facet>asList(FILTER_PRICE));
        final List<FilterExpression> filters = FilterParser.parse(queryString, Arrays.<Filter>asList(FILTER_SEARCH));
        SearchRequest<Product> searchRequest = sphere.products().filter(locale, filters).facet(facets)
                .sort(parameters.getSort()).page(parameters.getPage()).pageSize(parameters.getPageSize());
        return searchRequest.fetchAsync()
                .map(new F.Function<SearchResult<Product>, ProductList>() {
                @Override
                public ProductList apply(SearchResult<Product> result) throws Throwable {
                    parameters.withFilters(filters).withFacets(facets);
                    return ProductList.of(result, parameters);
                }
            });
    }

    @Override
    public F.Promise<ProductList> fetchCategoryProducts(final Locale locale, final Map<String, String[]> queryString,
                                                        final ShopCategory category, final RequestParameters parameters) {
        // TODO try to use facets generically
        final List<FilterExpression> filters = Arrays.asList(filterExpressionOnlyCategory(category));
        final List<FacetExpression> facets = FacetParser.parse(queryString, Arrays.<Facet>asList(FILTER_PRICE));
        SearchRequest<Product> searchRequest = sphere.products().filter(locale, filters).facet(facets)
                .sort(parameters.getSort()).page(parameters.getPage()).pageSize(parameters.getPageSize());
        return searchRequest.fetchAsync()
                .map(new F.Function<SearchResult<Product>, ProductList>() {
                    @Override
                    public ProductList apply(SearchResult<Product> result) throws Throwable {
                        parameters.withFilters(filters).withFacets(facets);
                        return ProductList.of(result, parameters);
                    }
                });
    }

    protected static FilterExpression filterExpressionOnlySku(String sku) {
        List<String> skuList = Arrays.asList(sku);
        return new FilterExpressions.StringAttribute.EqualsAnyOf(FILTER_ATTRIBUTE_SKU, skuList);
    }

    protected static FilterExpression filterExpressionOnlyCategory(ShopCategory category) {
        List<Category> categories = Arrays.asList(category.get());
        return new FilterExpressions.CategoriesOrSubcategories(categories);
    }

    protected static Facets.MoneyAttribute.Ranges filterRequestPrice() {
        List<Range<BigDecimal>> ranges = Collections.singletonList(Range.greaterThan(BigDecimal.ZERO));
        return new Facets.MoneyAttribute.Ranges(FILTER_ATTRIBUTE_PRICE, ranges).setQueryParam(FILTER_PRICE_QUERY_PARAM);
    }

    protected static Filters.Fulltext filterRequestSearch() {
        return new Filters.Fulltext().setQueryParam(FILTER_SEARCH_QUERY_PARAM);
    }
}
