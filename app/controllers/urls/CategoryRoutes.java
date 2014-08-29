package controllers.urls;

import controllers.routes;
import play.i18n.Lang;
import play.mvc.Call;

import java.util.List;

public class CategoryRoutes {
    private final List<Lang> availableLang;

    CategoryRoutes(List<Lang> availableLang) {
        this.availableLang = availableLang;
    }

    /*
    public Map<Lang, Call> get(CustomCategory currentCategory) {
        Map<Lang, Call> localizedUrls = new HashMap<>();
        for (Lang lang : availableLang) {
            Call call = byCategory(lang.toLocale(), currentCategory);
            // TODO build query facets
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public Call byCategory(Locale locale, CustomCategory category) {
        return bySlug(category.getSlug(locale));
    }
    */

    /**
     * Gets the category URL call for the provided slug.
     * @param categorySlug the localized category slug.
     * @return the URL call for the category.
     */
    public Call bySlug(String categorySlug) {
        return routes.Categories.select(categorySlug, 1, 24, "", "");
    }
}
