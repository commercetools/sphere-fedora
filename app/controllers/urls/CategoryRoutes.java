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
    private final Locale currentLocale;
    private final List<Lang> availableLang;

    CategoryRoutes(Locale currentLocale, List<Lang> availableLang) {
        this.currentLocale = currentLocale;
        this.availableLang = availableLang;
    }

    public Map<Lang, Call> all(ShopCategory currentCategory) {
        Map<Lang, Call> localizedUrls = new HashMap<Lang, Call>();
        for (Lang lang : availableLang) {
            Call call = get(lang.toLocale(), currentCategory);
            // TODO build query facets
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public Call get(ShopCategory category) {
        return get(currentLocale, category);
    }

    public Call get(Locale locale, ShopCategory category) {
        return bySlug(category.getSlug(locale));
    }

    /**
     * Gets the category URL call for the provided slug.
     * @param categorySlug the localized category slug.
     * @return the URL call for the category.
     */
    public Call bySlug(String categorySlug) {
        return routes.ProductListController.categoryProducts(categorySlug, 1);
    }
}
