package controllers.urls;

import controllers.routes;
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

    public Map<Lang, ShopCall> all(ShopCategory currentCategory) {
        Map<Lang, ShopCall> localizedUrls = new HashMap<Lang, ShopCall>();
        for (Lang lang : availableLang) {
            ShopCall call = get(lang.toLocale(), currentCategory);
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public ShopCall get(ShopCategory category) {
        return get(currentLocale, category);
    }

    public ShopCall get(Locale locale, ShopCategory category) {
        return bySlug(category.getSlug(locale));
    }

    /**
     * Gets the category URL call for the provided slug.
     * @param categorySlug the localized category slug.
     * @return the URL call for the category.
     */
    public ShopCall bySlug(String categorySlug) {
        return new ShopCall(routes.ProductListController.categoryProducts(categorySlug, 1));
    }
}
