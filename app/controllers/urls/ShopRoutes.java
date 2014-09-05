package controllers.urls;

import play.i18n.Lang;

import java.util.List;
import java.util.Locale;

public class ShopRoutes {
    private final Locale currentLocale;
    private final List<Lang> availableLang;

    private ShopRoutes(Locale currentLocale, List<Lang> availableLang) {
        this.currentLocale = currentLocale;
        this.availableLang = availableLang;
    }

    public static ShopRoutes of(Locale currentLocale, List<Lang> availableLang) {
        return new ShopRoutes(currentLocale, availableLang);
    }

    public CategoryRoutes categories() {
        return new CategoryRoutes(currentLocale, availableLang);
    }

    public ProductRoutes products() {
        return new ProductRoutes(currentLocale, availableLang);
    }

    public OtherRoutes any() {
        return new OtherRoutes(availableLang);
    }
}
