package models;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import controllers.urls.ShopRoutes;
import play.i18n.Lang;

public final class CommonDataBuilder {
    private UserContext userContext;
    private List<Lang> availableLang;
    private List<CountryCode> availableCountry;
    private List<ShopCategory> rootCategories;
    private Optional<ShopCategory> currentCategory = Optional.absent();
    private Optional<ShopProduct> currentProduct = Optional.absent();
    private RequestParameters requestParameters = RequestParameters.empty();
    private List<String> selectableAttributeNames = Collections.emptyList();

    private CommonDataBuilder(final UserContext userContext, final List<Lang> availableLang, final List<CountryCode> availableCountry,
                              final List<ShopCategory> rootCategories) {
        this.userContext = userContext;
        this.availableLang = availableLang;
        this.availableCountry = availableCountry;
        this.rootCategories = rootCategories;
    }

    public static CommonDataBuilder of(final UserContext userContext, final List<Lang> availableLang,
                                       final List<CountryCode> availableCountry, final List<ShopCategory> rootCategories) {
        return new CommonDataBuilder(userContext, availableLang, availableCountry, rootCategories);
    }

    public CommonData build() {
        ShopRoutes shopRoutes = ShopRoutes.of(userContext.locale(), availableLang);
        return new CommonData(userContext, availableLang, availableCountry, shopRoutes, requestParameters, rootCategories,
                currentCategory, currentProduct, selectableAttributeNames);
    }

    public CommonDataBuilder withCategory(ShopCategory category) {
        this.currentCategory = Optional.of(category);
        return this;
    }

    public CommonDataBuilder withProduct(ShopProduct product) {
        this.currentProduct = Optional.of(product);
        return this;
    }

    public CommonDataBuilder withRequestParameters(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
        return this;
    }

    public CommonDataBuilder withSelectableAttributes(List<String> selectableAttributeNames) {
        this.selectableAttributeNames = selectableAttributeNames;
        return this;
    }
}
