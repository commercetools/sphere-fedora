package models;

import com.google.common.base.Optional;
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
        return !parameters.getPriceParameter().isPresent();
    }

    public boolean hasNoDisplaySelected() {
        return !parameters.getDisplayParameter().isPresent();
    }

    public boolean hasPriceSelected(String price) {
        Optional<String> selectedPrice = parameters.getPriceParameter();
        return selectedPrice.isPresent() && selectedPrice.get().equals(price);
    }

    public boolean hasSortSelected(String sort) {
        Optional<String> selectedSort = parameters.getSortParameter();
        return selectedSort.isPresent() && selectedSort.get().equals(sort);
    }

    public boolean hasDisplaySelected(String display) {
        Optional<String> selectedDisplay = parameters.getDisplayParameter();
        return selectedDisplay.isPresent() && selectedDisplay.get().equals(display);
    }

    public boolean hasAmountSelected(String amount) {
        Optional<String> selectedAmount = parameters.getAmountParameter();
        return selectedAmount.isPresent() && selectedAmount.get().equals(amount);
    }
}
