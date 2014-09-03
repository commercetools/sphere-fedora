package models;

import io.sphere.client.ProductSort;
import io.sphere.client.facets.expressions.FacetExpression;
import io.sphere.client.filters.expressions.FilterExpression;

import java.util.ArrayList;
import java.util.List;

public class RequestParameters {
    public static final String FILTER_SEARCH_QUERY_PARAM = "q";
    public static final String FILTER_PRICE_QUERY_PARAM = "price";
    public static final String SORT_PRICE_DESC_QUERY_PARAM = "price_desc";
    public static final String SORT_PRICE_ASC_QUERY_PARAM = "price_asc";
    public static final String DISPLAY_GRID_QUERY_PARAM = "grid";

    private final int page;
    private final int pageSize;
    private final ProductSort sort;
    private final boolean grid;
    private List<FilterExpression> filters = new ArrayList<>();
    private List<FacetExpression> facets = new ArrayList<>();

    RequestParameters(int page, int pageSize, ProductSort sort, boolean grid) {
        this.page = page;
        this.pageSize = pageSize;
        this.sort = sort;
        this.grid = grid;
    }

    public static RequestParameters of(int pageParameter, int pageSizeParameter, String sortParameter,
                                       String displayParameter) {
        ProductSort sort = parseSortParameter(sortParameter);
        int page = parsePageParameter(pageParameter);
        int pageSize = parsePageSizeParameter(pageSizeParameter);
        boolean grid = parseGridParameter(displayParameter);
        return new RequestParameters(page, pageSize, sort, grid);
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
        if (sortParameter.equals(SORT_PRICE_ASC_QUERY_PARAM)) {
            return ProductSort.price.asc;
        } else if (sortParameter.equals(SORT_PRICE_DESC_QUERY_PARAM)) {
            return ProductSort.price.desc;
        } else {
            return ProductSort.name.asc;
        }
    }

    private static boolean parseGridParameter(final String displayParameter) {
        return displayParameter.equals(DISPLAY_GRID_QUERY_PARAM);
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

    public List<FilterExpression> getFilters() {
        return filters;
    }

    public List<FacetExpression> getFacets() {
        return facets;
    }

    public RequestParameters withFilters(List<FilterExpression> filters) {
        this.filters = filters;
        return this;
    }

    public RequestParameters withFacets(List<FacetExpression> facets) {
        this.facets = facets;
        return this;
    }

    @Override
    public String toString() {
        return "RequestParameters{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", sort=" + sort +
                ", grid=" + grid +
                ", filters=" + filters +
                ", facets=" + facets +
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
        if (facets != null ? !facets.equals(that.facets) : that.facets != null) return false;
        if (filters != null ? !filters.equals(that.filters) : that.filters != null) return false;
        if (sort != null ? !sort.equals(that.sort) : that.sort != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = page;
        result = 31 * result + pageSize;
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (grid ? 1 : 0);
        result = 31 * result + (filters != null ? filters.hashCode() : 0);
        result = 31 * result + (facets != null ? facets.hashCode() : 0);
        return result;
    }
}
