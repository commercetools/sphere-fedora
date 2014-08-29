package controllers.urls;

import play.i18n.Lang;

import java.util.List;

public class ShopRoutes {
    private final List<Lang> availableLang;

    private ShopRoutes(List<Lang> availableLang) {
        this.availableLang = availableLang;
    }

    public static ShopRoutes of(List<Lang> availableLang) {
        return new ShopRoutes(availableLang);
    }

    public CategoryRoutes categories() {
        return new CategoryRoutes(availableLang);
    }

    public ProductRoutes products() {
        return new ProductRoutes(availableLang);
    }

    /*
    public Map<Lang, Call> get(Optional<CustomCategory> currentCategory, Optional<CustomVariant> currentVariant) {
        if (currentVariant.isPresent()) {
            return products().get(currentVariant.get(), currentCategory);
        } else if (currentCategory.isPresent()) {
            return categories().get(currentCategory.get());
        } else {
            // TODO implement other page case
            return new HashMap<>();
        }
    }
    */
}
