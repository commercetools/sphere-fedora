package controllers.urls;

import controllers.routes;
import models.RequestParameters;
import models.ShopCategory;
import play.i18n.Lang;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryRoutes {
    private final Locale currentLocale;
    private final List<Lang> availableLang;

    CategoryRoutes(Locale currentLocale, List<Lang> availableLang) {
        this.currentLocale = currentLocale;
        this.availableLang = availableLang;
    }

    public Map<Lang, ShopCall> all(ShopCategory currentCategory, RequestParameters parameters) {
        Map<Lang, ShopCall> localizedUrls = new HashMap<Lang, ShopCall>();
        for (Lang lang : availableLang) {
            Locale languageLocale = new Locale(lang.language());
            ShopCall call = get(languageLocale, currentCategory, parameters);
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public ShopCall get(ShopCategory category, RequestParameters parameters) {
        return get(currentLocale, category, parameters);
    }

    public ShopCall get(Locale locale, ShopCategory category, RequestParameters parameters) {
        return bySlug(category.getSlug(locale), parameters);
    }

    /**
     * Gets the category URL call for the provided slug.
     * @param categorySlug the localized category slug.
     * @param parameters the request parameters associated with the current request.
     * @return the URL call for the category.
     */
    public ShopCall bySlug(String categorySlug, RequestParameters parameters) {
        ShopCall call = ShopCall.of(routes.ProductListController.categoryProducts(categorySlug, 1));
        return call.withFilters(parameters);
    }
}
