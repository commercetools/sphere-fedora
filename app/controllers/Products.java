package controllers;

import controllers.actions.SaveContext;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.filters.expressions.FilterExpressions;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import io.sphere.client.shop.model.Variant;
import play.mvc.Result;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.productDetail;

import java.util.Collections;


@With(SaveContext.class)
public class Products extends BaseController {

    public Products(CategoryService categoryService, ProductService productService, CartService cartService, CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    public Result select(String productSlug, int variantId) {
        Product product = sphere().products().bySlug(lang().toLocale(), productSlug).fetch().orNull();
        if (product == null) {
            return notFound("Product not found: " + productSlug);
        }
        return renderProduct(variantId, product);
    }

    //TODO this should not be indexed by search engines
    public Result selectById(String productId, int variantId) {
        Product product = sphere().products().byId(productId).fetch().orNull();
        if (product == null) {
            return notFound("Product not found: " + productId);
        }
        return renderProduct(variantId, product);
    }

    private Result renderProduct(int variantId, Product product) {
        Variant variant = product.getVariants().byId(variantId).or(product.getMasterVariant());
        Category category = product.getCategories().get(0);
        FilterExpression categoryFilter = new FilterExpressions.Categories(Collections.singletonList(category));
        SearchResult<Product> searchResult = sphere().products().filter(lang().toLocale(), categoryFilter).fetch();
        return ok(productDetail.render(data().build(), product, variant, category, searchResult));
    }
}
