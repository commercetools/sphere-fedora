package controllers.urls;

import java.util.List;

import com.google.common.base.Optional;
import play.i18n.Lang;
import play.mvc.Call;
import controllers.routes;

public class ProductRoutes {
    private final List<Lang> availableLang;

    ProductRoutes(List<Lang> availableLang) {
        this.availableLang = availableLang;
    }

    /*
    public Map<Lang, Call> get(CustomVariant variant, Optional<CustomCategory> category) {
        Map<Lang, Call> localizedUrls = new HashMap<>();
        for (Lang lang : availableLang) {
            Call call = byVariant(lang.toLocale(), variant, category);
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public Call byVariant(Locale locale, CustomVariant variant, Optional<CustomCategory> category) {
        Optional<String> categorySlug = Optional.absent();
        if (category.isPresent()) {
            categorySlug = Optional.of(category.get().getSlug(locale));
        }
        return bySlug(variant.getSlug(locale), variant.getId(), categorySlug);
    }*/

    /**
     * Gets the product URL call for the provided slug and variant ID.
     * @param productSlug the localized product slug.
     * @param variantId the internal identifier of the variant.
     * @param categorySlug the localized category slug to which the product is associated for this call.
     * @return the URL call for the product.
     */
    public Call bySlug(String productSlug, int variantId, Optional<String> categorySlug) {
        return routes.Products.select(productSlug, variantId);
    }
}
