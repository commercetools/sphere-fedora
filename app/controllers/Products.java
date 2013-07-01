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
import sphere.SearchRequest;
import sphere.ShopController;
import views.html.productDetail;


@With(SaveContext.class)
public class Products extends ShopController {

    public static Result select(String productSlug, int variantId) {
        // Case invalid product
        Product product = sphere().products().bySlug(productSlug).fetch().orNull();
        if (product == null) {
            return notFound("Product not found: " + productSlug);
        }
        // Case valid select product
        Variant variant = product.getVariants().byId(variantId).or(product.getMasterVariant());
        Category category = product.getCategories().get(0);
        FilterExpression categoryFilter = new FilterExpressions.Categories(category);
        SearchResult<Product> searchResult = sphere().products().filter(categoryFilter).fetch();
        return ok(productDetail.render(product, variant, category, searchResult));
    }
}
