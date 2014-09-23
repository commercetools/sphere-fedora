package controllers.urls;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Optional;
import models.ShopCategory;
import models.ShopProduct;
import models.ShopVariant;
import play.i18n.Lang;
import controllers.routes;

public class ProductRoutes {
    private final Locale currentLocale;
    private final List<Lang> availableLang;

    ProductRoutes(Locale currentLocale, List<Lang> availableLang) {
        this.currentLocale = currentLocale;
        this.availableLang = availableLang;
    }

    public Map<Lang, ShopCall> all(ShopProduct product, Optional<ShopCategory> category) {
        Map<Lang, ShopCall> localizedUrls = new HashMap<Lang, ShopCall>();
        for (Lang lang : availableLang) {
            Locale languageLocale = new Locale(lang.language());
            ShopCall call = get(languageLocale, product, product.getSelectedVariant(), category);
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    public ShopCall get(ShopProduct product, ShopVariant variant, Optional<ShopCategory> category) {
        return get(currentLocale, product, variant, category);
    }

    public ShopCall get(Locale locale, ShopProduct product, ShopVariant variant, Optional<ShopCategory> category) {
        Optional<String> categorySlug = Optional.absent();
        if (category.isPresent()) {
            categorySlug = Optional.of(category.get().getSlug(locale));
        }
        return bySlug(product.getSlug(locale), variant.getId(), categorySlug);
    }

    /**
     * Gets the product URL call for the provided slug and variant ID.
     * @param productSlug the localized product slug.
     * @param variantId the internal identifier of the variant.
     * @param categorySlug the localized category slug to which the product is associated for this call.
     * @return the URL call for the product.
     */
    public ShopCall bySlug(String productSlug, int variantId, Optional<String> categorySlug) {
        return ShopCall.of(routes.ProductController.select(productSlug, variantId, categorySlug.or("")));
    }
}
