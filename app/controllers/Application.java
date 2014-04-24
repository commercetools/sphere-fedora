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
}
