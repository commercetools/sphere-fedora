package controllers.urls;

import controllers.routes;
import models.ShopCategory;
import play.i18n.Lang;
import play.mvc.Call;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryRoutes {
    private final List<Lang> availableLang;

    CategoryRoutes(List<Lang> availableLang) {
        this.availableLang = availableLang;
    }

    public Map<Lang, Call> get(ShopCategory currentCategory) {
        Map<Lang, Call> localizedUrls = new HashMap<>();
        for (Lang lang : availableLang) {
            Call call = byCategory(lang.toLocale(), currentCategory);
            // TODO build query facets
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public Call byCategory(Locale locale, ShopCategory category) {
        return bySlug(category.getSlug(locale));
    }

    /**
     * Gets the category URL call for the provided slug.
     * @param categorySlug the localized category slug.
     * @return the URL call for the category.
     */
    public Call bySlug(String categorySlug) {
        return routes.Categories.select(categorySlug, 1, 24, "", "");
    }
}
