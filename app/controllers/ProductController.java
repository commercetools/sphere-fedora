package controllers;

import com.google.common.base.Optional;
import controllers.actions.SaveContext;
import models.*;
import play.libs.F;
import play.mvc.Result;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;

import static utils.AsyncUtils.asPromise;

@With(SaveContext.class)
public class ProductController extends BaseController {

    public ProductController(CategoryService categoryService, ProductService productService,
                             CartService cartService, CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    public F.Promise<Result> select(final String productSlug, final int variantId, final String categorySlug) {
        return productService().fetchBySlug(locale(), productSlug, variantId)
                .flatMap(new F.Function<Optional<ShopProduct>, F.Promise<Result>>() {
                    @Override
                    public F.Promise<Result> apply(final Optional<ShopProduct> product) throws Throwable {
                        if (product.isPresent()) {
                            final Optional<ShopCategory> category = categoryService().getBySlug(locale(), categorySlug)
                                    .or(product.get().getMainCategory());
                            return productService().fetchRecommendedProducts(locale(), product.get())
                                    .map(new F.Function<Optional<ProductList>, Result>() {
                                        @Override
                                        public Result apply(Optional<ProductList> productList) throws Throwable {
                                            return showProductPage(data(), product.get(), category, productList);
                                        }
                                    });
                        } else {
                            return asPromise(notFound(showNotFoundPage(data())));
                        }
                    }
                });
    }

    //TODO this should not be indexed by search engines
    public F.Promise<Result> selectById(String productId, int variantId) {
        return productService().fetchById(productId, variantId)
                .flatMap(new F.Function<Optional<ShopProduct>, F.Promise<Result>>() {
                    @Override
                    public F.Promise<Result> apply(final Optional<ShopProduct> product) throws Throwable {
                        final Optional<ShopCategory> category = product.get().getMainCategory();
                        if (product.isPresent()) {
                            return productService().fetchRecommendedProducts(locale(), product.get())
                                    .map(new F.Function<Optional<ProductList>, Result>() {
                                        @Override
                                        public Result apply(Optional<ProductList> productList) throws Throwable {
                                            return showProductPage(data(), product.get(), category, productList);
                                        }
                                    });
                        } else {
                            return asPromise(notFound(showNotFoundPage(data())));
                        }
                    }
                });
    }

    static Result showProductPage(CommonDataBuilder dataBuilder, ShopProduct product, Optional<ShopCategory> category,
                                  Optional<ProductList> recommendedProducts) {
        if (category.isPresent()) {
            dataBuilder.withCategory(category.get());
        }
        CommonData data = dataBuilder.withProduct(product).build();
        return ok(views.html.productDetailView.render(data, product, recommendedProducts));
    }
}
