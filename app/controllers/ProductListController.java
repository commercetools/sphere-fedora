package controllers;

import com.google.common.base.Optional;
import controllers.actions.SaveContext;
import models.*;
import play.libs.F;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import services.*;

import static utils.AsyncUtils.asPromise;

public class ProductListController extends BaseController {

    public ProductListController(CategoryService categoryService, ProductService productService,
                                 CartService cartService, CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    //since this is the default endpoint if any other route does not match, it must not
    //have the @With(SaveContext.class) annotation since this returns 404
    public F.Promise<Result> categoryProducts(String categorySlug, int page) {
        final Optional<ShopCategory> category = categoryService().getBySlug(locale(), categorySlug);
        if (category.isPresent()) {
            RequestParameters parameters = RequestParameters.of(queryString(), page);
            return productService().fetchCategoryProducts(locale(), category.get(), parameters)
                    .map(new F.Function<ProductList, Result>() {
                        @Override
                        public Result apply(ProductList productList) throws Throwable {
                            if (productList.isEmpty()) {
                                flash("info-product-list", "No products found");
                            }
                            session().put("returnUrl", request().uri());
                            return ok(showProductCategoryPage(data(), productList, category.get()));
                        }
                    });
        } else {
            return asPromise(notFound(showNotFoundPage(data())));
        }
    }

    @With(SaveContext.class)
    public F.Promise<Result> searchProducts(int page) {
        final RequestParameters parameters = RequestParameters.of(queryString(), page);
        return productService().fetchSearchedProducts(locale(), parameters)
                .map(new F.Function<ProductList, Result>() {
                    @Override
                    public Result apply(ProductList productList) throws Throwable {
                        if (productList.isEmpty()) {
                            flash("info-product-list", "No products found");
                        }
                        return ok(showProductSearchPage(data(), productList));
                    }
                });

    }

    @With(SaveContext.class)
    static Content showProductCategoryPage(CommonDataBuilder dataBuilder, ProductList productList, ShopCategory category) {
        CommonData data = dataBuilder.withCategory(category).build();
        return views.html.productListView.render(data, productList);
    }

    @With(SaveContext.class)
    static Content showProductSearchPage(CommonDataBuilder dataBuilder, ProductList productList) {
        return views.html.productListView.render(dataBuilder.build(), productList);
    }
}
