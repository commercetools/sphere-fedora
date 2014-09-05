package controllers.urls;

import models.RequestParameters;
import play.i18n.Lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherRoutes {
    private final List<Lang> availableLang;

    OtherRoutes(List<Lang> availableLang) {
        this.availableLang = availableLang;
    }

    public Map<Lang, ShopCall> all(RequestParameters parameters) {
        Map<Lang, ShopCall> localizedUrls = new HashMap<Lang, ShopCall>();
        for (Lang lang : availableLang) {
            ShopCall call = get(parameters);
            localizedUrls.put(lang, call);
        }
        return localizedUrls;
    }

    /**
     * Gets the search URL call for the provided slug.
     * @param parameters the request parameters associated with the current request.
     * @return the URL call for the search.
     */
    public ShopCall get(RequestParameters parameters) {
        return ShopCall.empty().withFilters(parameters);
    }
}
