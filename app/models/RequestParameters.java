package models;

import com.google.common.collect.Range;
import io.sphere.client.ProductSort;
import io.sphere.client.facets.Facet;
import io.sphere.client.facets.FacetParser;
import io.sphere.client.facets.Facets;
import io.sphere.client.facets.expressions.FacetExpression;
import io.sphere.client.filters.Filter;
import io.sphere.client.filters.FilterParser;
import io.sphere.client.filters.Filters;
import io.sphere.client.filters.expressions.FilterExpression;

import java.math.BigDecimal;
import java.util.*;

public class RequestParameters {
    protected static final String QUERY_PARAM_SEARCH = "query";
    protected static final String QUERY_PARAM_PRICE = "price";

    protected static final String QUERY_SORT_PRICE_DESC = "price_desc";
    protected static final String QUERY_SORT_PRICE_ASC = "price_asc";
    protected static final String QUERY_DISPLAY_GRID = "grid";

    private final Map<String, String[]> queryString;
    private final int page;
    private final int pageSize;
    private final ProductSort sort;
    private final boolean grid;

    public final Filters.Fulltext filterSearch = buildSearchFilter();
    public final Facets.MoneyAttribute.Ranges facetPrice = buildPriceFacet();

    RequestParameters(Map<String, String[]> queryString, int page, int pageSize, ProductSort sort, boolean grid) {
        this.queryString = queryString;
        this.page = page;
        this.pageSize = pageSize;
        this.sort = sort;
        this.grid = grid;
    }

    public static RequestParameters of(Map<String, String[]> queryString, int pageParameter, int pageSizeParameter,
                                       String sortParameter, String displayParameter) {
        ProductSort sort = parseSortParameter(sortParameter);
        int page = parsePageParameter(pageParameter);
        int pageSize = parsePageSizeParameter(pageSizeParameter);
        boolean grid = parseGridParameter(displayParameter);
        return new RequestParameters(queryString, page, pageSize, sort, grid);
    }

    private static int parsePageSizeParameter(final int pageSizeParameter) {
        if (pageSizeParameter != 24) {
            return 12;
        } else {
            return pageSizeParameter;
        }
    }

    private static int parsePageParameter(final int pageParameter) {
        int page;
        if (pageParameter < 1) {
            page = 1;
        } else {
            page = pageParameter;
        }
        // Convert page from 1..N to 0..N-1
        return page - 1;
    }

    private static ProductSort parseSortParameter(final String sortParameter) {
        if (sortParameter.equals(QUERY_SORT_PRICE_ASC)) {
            return ProductSort.price.asc;
        } else if (sortParameter.equals(QUERY_SORT_PRICE_DESC)) {
            return ProductSort.price.desc;
        } else {
            return ProductSort.name.asc;
        }
    }

    private static boolean parseGridParameter(final String displayParameter) {
        return displayParameter.equals(QUERY_DISPLAY_GRID);
    }

    public Map<String, String[]> getQueryString() {
        return queryString;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public ProductSort getSort() {
        return sort;
    }

    public boolean isGrid() {
        return grid;
    }

    public Filters.Fulltext getFilterSearch() {
        return filterSearch;
    }

    public Facets.MoneyAttribute.Ranges getFacetPrice() {
        return facetPrice;
    }

    public List<String> getSelectedPriceFilter() {
        List<String> selectedPrices = new ArrayList<String>();
        if (queryString.containsKey(QUERY_PARAM_PRICE)) {
            selectedPrices = Arrays.asList(queryString.get(QUERY_PARAM_PRICE));
        }
        return selectedPrices;
    }

    public List<FilterExpression> getFilters() {
        final List<Filter> usedFilters = Arrays.<Filter>asList(filterSearch);
        return FilterParser.parse(queryString, usedFilters);
    }

    public List<FacetExpression> getFacets() {
        final List<Facet> usedFacets = Arrays.<Facet>asList(facetPrice);
        return FacetParser.parse(queryString, usedFacets);
    }

    protected static Facets.MoneyAttribute.Ranges buildPriceFacet() {
        List<Range<BigDecimal>> ranges = Collections.singletonList(Range.greaterThan(BigDecimal.ZERO));
        return new Facets.MoneyAttribute.Ranges("variants.price", ranges).setQueryParam(QUERY_PARAM_PRICE);
    }

    protected static Filters.Fulltext buildSearchFilter() {
        return new Filters.Fulltext().setQueryParam(QUERY_PARAM_SEARCH);
    }

    @Override
    public String toString() {
        return "RequestParameters{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", sort=" + sort +
                ", grid=" + grid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestParameters that = (RequestParameters) o;

        if (grid != that.grid) return false;
        if (page != that.page) return false;
        if (pageSize != that.pageSize) return false;
        if (sort != null ? !sort.equals(that.sort) : that.sort != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = page;
        result = 31 * result + pageSize;
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (grid ? 1 : 0);
        return result;
    }
}
