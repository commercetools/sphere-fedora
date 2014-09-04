package models;

import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductList {
    private final SearchResult<Product> searchResult;
    private final RequestParameters parameters;

    ProductList(SearchResult<Product> searchResult, RequestParameters parameters) {
        this.searchResult = searchResult;
        this.parameters = parameters;
    }

    public static ProductList of(SearchResult<Product> searchResult, RequestParameters parameters) {
        return new ProductList(searchResult, parameters);
    }

    public SearchResult<Product> get() {
        return searchResult;
    }

    public RequestParameters getParameters() {
        return parameters;
    }

    public List<ShopProduct> getProducts() {
        List<ShopProduct> products = new ArrayList<ShopProduct>();
        for (Product product : searchResult.getResults()) {
            products.add(ShopProduct.of(product));
        }
        return products;
    }

    public boolean isEmpty() {
        return searchResult.getResults().isEmpty();
    }

    public boolean hasNoPriceSelected() {
        return parameters.getSelectedPriceFilter().isEmpty();
    }

    public boolean hasPriceSelected(String price) {
        return parameters.getSelectedPriceFilter().contains(price);
    }
}
