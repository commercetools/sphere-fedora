package controllers;

import controllers.actions.SaveContext;
import io.sphere.client.ProductSort;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.Product;
import play.mvc.Result;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.index;

public class Application extends BaseController {

    public Application(final CategoryService categoryService, final ProductService productService,
                       final CartService cartService, final CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    @With(SaveContext.class)
    public Result home() {
        SearchResult<Product> searchResultNew = sphere().products().all(lang().toLocale()).sort(ProductSort.price.desc).fetch();
        SearchResult<Product> searchResultOffer = sphere().products().all(lang().toLocale()).sort(ProductSort.price.asc).fetch();
        return ok(index.render(data().build(), searchResultNew, searchResultOffer));
    }
}
