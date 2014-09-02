package controllers.urls;

import com.google.common.base.Optional;
import models.ShopCategory;
import models.ShopProduct;
import play.i18n.Lang;
import play.mvc.Call;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Lang, Call> get(Optional<ShopCategory> currentCategory, Optional<ShopProduct> currentProduct) {
        if (currentProduct.isPresent()) {
            return products().get(currentProduct.get(), currentCategory);
        } else if (currentCategory.isPresent()) {
            return categories().get(currentCategory.get());
        } else {
            // TODO implement other page case
            return new HashMap<>();
        }
    }
}
