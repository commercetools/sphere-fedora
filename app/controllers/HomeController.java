package controllers;

import controllers.actions.SaveContext;
import models.CommonDataBuilder;
import models.ProductList;
import play.libs.F;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;

public class HomeController extends BaseController {

    public HomeController(final CategoryService categoryService, final ProductService productService,
                          final CartService cartService, final CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    @With(SaveContext.class)
    public F.Promise<Result> home() {
        return productService().fetchNewProducts().zip(productService().fetchProductsInOffer())
                .map(new F.Function<F.Tuple<ProductList, ProductList>, Result>() {
                    @Override
                    public Result apply(F.Tuple<ProductList, ProductList> productLists) throws Throwable {
                        ProductList productsNewest = productLists._1;
                        ProductList productsOffers = productLists._2;
                        return ok(showHomePage(data(), productsNewest, productsOffers));
                    }
                });
    }

    static Content showHomePage(CommonDataBuilder dataBuilder, ProductList productsNewest, ProductList productsOffers) {
        return views.html.homeView.render(dataBuilder.build(), productsNewest, productsOffers);
    }
}
