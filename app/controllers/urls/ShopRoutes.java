package controllers.urls;

import com.google.common.base.Optional;
import models.ShopCategory;
import models.ShopProduct;
import play.i18n.Lang;
import play.mvc.Call;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public Map<Lang, Call> all(Optional<ShopCategory> currentCategory, Optional<ShopProduct> currentProduct) {
        if (currentProduct.isPresent()) {
            return products().all(currentProduct.get(), currentCategory);
        } else if (currentCategory.isPresent()) {
            return categories().all(currentCategory.get());
        } else {
            // TODO implement other page case
            return new HashMap<>();
        }
    }
}
