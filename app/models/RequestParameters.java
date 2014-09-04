package models;

import com.google.common.base.Optional;
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
    public static final String QUERY_PARAM_SORT = "sort";
    public static final String QUERY_PARAM_SEARCH = "query";
    public static final String QUERY_PARAM_PRICE = "price";
    public static final String QUERY_PARAM_DISPLAY = "display";
    public static final String QUERY_PARAM_AMOUNT = "amount";

    protected static final String SORT_PRICE_ASC = "price_asc";
    protected static final String SORT_PRICE_DESC = "price_desc";

    protected static final String DISPLAY_LIST = "list";

    protected static final String AMOUNT_LARGE = "large";

    private final Map<String, String[]> queryString;
    private final int page;

    public final Filters.Fulltext filterSearch = buildSearchFilter();
    public final Facets.MoneyAttribute.Ranges facetPrice = buildPriceFacet();

    RequestParameters(Map<String, String[]> queryString, int page) {
        this.queryString = queryString;
        this.page = page;
    }

    public static RequestParameters of(Map<String, String[]> queryString, int pageParameter) {
        /* Convert page from 1..N to 0..N-1 */
        int page = 0;
        if (pageParameter > 1) {
            page = pageParameter - 1;
        }
        return new RequestParameters(queryString, page);
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        Optional<String> amountParameter = getAmountParameter();
        if (amountParameter.isPresent() && amountParameter.get().equals(AMOUNT_LARGE)) {
            return 24;
        } else {
            return 12;
        }
    }

    public ProductSort getSort() {
        Optional<String> sortParameter = getSortParameter();
        if (sortParameter.isPresent()) {
            if (sortParameter.get().equals(SORT_PRICE_ASC)) {
                return ProductSort.price.asc;
            } else if (sortParameter.get().equals(SORT_PRICE_DESC)) {
                return ProductSort.price.desc;
            }
        }
        return ProductSort.name.asc;
    }

    public boolean isList() {
        Optional<String> selectedDisplay = getDisplayParameter();
        return selectedDisplay.isPresent() && selectedDisplay.get().equals(DISPLAY_LIST);
    }

    public Filters.Fulltext getFilterSearch() {
        return filterSearch;
    }

    public Facets.MoneyAttribute.Ranges getFacetPrice() {
        return facetPrice;
    }

    public List<FilterExpression> getFilters() {
        final List<Filter> usedFilters = Arrays.<Filter>asList(filterSearch);
        return FilterParser.parse(queryString, usedFilters);
    }

    public List<FacetExpression> getFacets() {
        final List<Facet> usedFacets = Arrays.<Facet>asList(facetPrice);
        return FacetParser.parse(queryString, usedFacets);
    }

    public Optional<String> getSortParameter() {
        return getParameterValue(QUERY_PARAM_SORT);
    }

    public Optional<String> getDisplayParameter() {
        return getParameterValue(QUERY_PARAM_DISPLAY);
    }

    public Optional<String> getAmountParameter() {
        return getParameterValue(QUERY_PARAM_AMOUNT);
    }

    public Optional<String> getPriceParameter() {
        return getParameterValue(QUERY_PARAM_PRICE);
    }

    protected Optional<String> getParameterValue(String queryParameterKey) {
        if (queryString.containsKey(queryParameterKey)) {
            String[] parameters = queryString.get(queryParameterKey);
            if (parameters != null && parameters.length > 0) {
                return Optional.of(parameters[0]);
            }
        }
        return Optional.absent();
    }

    public static List<String> sortOptions() {
        return Arrays.asList(SORT_PRICE_ASC, SORT_PRICE_DESC);
    }

    public static List<String> displayOptions() {
        return Arrays.asList(DISPLAY_LIST);
    }

    public static List<String> amountOptions() {
        return Arrays.asList(AMOUNT_LARGE);
    }

    public static List<String> priceOptions() {
        return Arrays.asList("20_60", "60_100", "100_500");
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
                "queryString=" + queryString +
                ", page=" + page +
                ", filterSearch=" + filterSearch +
                ", facetPrice=" + facetPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestParameters that = (RequestParameters) o;

        if (page != that.page) return false;
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryString != null ? queryString.hashCode() : 0;
        result = 31 * result + page;
        return result;
    }
}
