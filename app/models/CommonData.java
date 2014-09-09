package models;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import controllers.urls.ShopCall;
import controllers.urls.ShopRoutes;
import play.i18n.Lang;

/**
 * A container for stuff needed in almost every template.
 */
public class CommonData {
    private final UserContext userContext;
    private final List<Lang> availableLang;
    private final List<CountryCode> availableCountry;
    private final List<ShopCategory> rootCategories;
    private final Optional<ShopCategory> currentCategory;
    private final Optional<ShopProduct> currentProduct;
    private final ShopRoutes shopRoutes;
    private final RequestParameters requestParameters;
    private final List<String> selectableAttributeNames;

    CommonData(UserContext userContext, List<Lang> availableLang, List<CountryCode> availableCountry, ShopRoutes shopRoutes,
               RequestParameters requestParameters, List<ShopCategory> rootCategories, Optional<ShopCategory> currentCategory,
               Optional<ShopProduct> currentProduct, List<String> selectableAttributeNames) {
        this.userContext = userContext;
        this.availableLang = availableLang;
        this.availableCountry = availableCountry;
        this.rootCategories = rootCategories;
        this.currentCategory = currentCategory;
        this.currentProduct = currentProduct;
        this.shopRoutes = shopRoutes;
        this.requestParameters = requestParameters;
        this.selectableAttributeNames = selectableAttributeNames;
    }

    public UserContext context() {
        return userContext;
    }

    public List<Lang> availableLang() {
        return availableLang;
    }

    public List<CountryCode> availableCountry() {
        return availableCountry;
    }

    public ShopRoutes routes() {
        return shopRoutes;
    }

    public Map<Lang, ShopCall> localizedRoutes() {
        if (currentProduct.isPresent()) {
            return shopRoutes.products().all(currentProduct.get(), currentCategory);
        } else if (currentCategory.isPresent()) {
            return shopRoutes.categories().all(currentCategory.get(), requestParameters);
        } else {
            return shopRoutes.any().all(requestParameters);
        }
    }

    public ShopCall route() {
        return shopRoutes.any().get(requestParameters);
    }

    public ShopCall filterCategoryRoute(ShopCategory category) {
        return shopRoutes.categories().get(category, requestParameters);
    }

    public ShopCall categoryRoute(ShopCategory category) {
        return shopRoutes.categories().get(category, RequestParameters.empty());
    }

    public ShopCall productRoute(ShopProduct product, Optional<ShopCategory> category) {
        return shopRoutes.products().get(product, product.getSelectedVariant(), category);
    }

    public ShopCall variantRoute(ShopProduct product, ShopVariant variant) {
        return shopRoutes.products().get(product, variant, currentCategory);
    }

    public Optional<ShopCategory> currentCategory() {
        return currentCategory;
    }

    public Optional<ShopProduct> currentProduct() {
        return currentProduct;
    }

    public List<ShopCategory> rootCategories() {
        return rootCategories;
    }

    public boolean isInCategory(ShopCategory category) {
        return currentCategory.isPresent() && currentCategory.get().equals(category);
    }

    public boolean isInCategoryPath(ShopCategory category) {
        return currentCategory.isPresent() && currentCategory.get().hasInPath(category);
    }

    public List<String> selectableAttributeNames() {
        return selectableAttributeNames;
    }
}
