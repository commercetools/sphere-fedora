package controllers;

import controllers.actions.SaveContext;
import io.sphere.client.ProductSort;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.Product;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.index;

public class Application extends ShopController {

    @With(SaveContext.class)
    public static Result home() {
        SearchResult<Product> searchResultNew = sphere().products().all(lang().toLocale()).sort(ProductSort.price.desc).fetch();
        SearchResult<Product> searchResultOffer = sphere().products().all(lang().toLocale()).sort(ProductSort.price.asc).fetch();
        return ok(index.render(searchResultNew, searchResultOffer));
    }

    public static Result selectLang(String lang) {
        // Case url not defined
        String url = request().getHeader("referer");
        if (url == null) {
            url = "/";
        }
        // Case invalid language selected
        if (!changeLang(lang)) {
            flash("error", "Language cannot be changed to " + lang);
            return redirect(url);
        }
        // Case change language successfully
        return redirect(url);
    }


}
