package models;

import java.util.List;

import com.google.common.base.Optional;
import controllers.urls.ShopRoutes;
import play.i18n.Lang;

public final class CommonDataBuilder {
    private UserContext userContext;
    private List<Lang> availableLang;
    private List<ShopCategory> rootCategories;
    private Optional<ShopCategory> currentCategory = Optional.absent();
    private Optional<ShopProduct> currentProduct = Optional.absent();

    private CommonDataBuilder(final UserContext userContext, final List<Lang> availableLang,
                              List<ShopCategory> rootCategories) {
        this.userContext = userContext;
        this.availableLang = availableLang;
        this.rootCategories = rootCategories;
    }

    public static CommonDataBuilder of(final UserContext userContext, final List<Lang> availableLang,
                                       final List<ShopCategory> rootCategories) {
        return new CommonDataBuilder(userContext, availableLang, rootCategories);
    }

    public CommonData build() {
        ShopRoutes shopRoutes = ShopRoutes.of(userContext.locale(), availableLang);
        return new CommonData(userContext, availableLang, shopRoutes, rootCategories, currentCategory, currentProduct);
    }

    public CommonDataBuilder withCategory(ShopCategory category) {
        this.currentCategory = Optional.of(category);
        return this;
    }

    public CommonDataBuilder withProduct(ShopProduct product) {
        this.currentProduct = Optional.of(product);
        return this;
    }
}
