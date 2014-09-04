package controllers;

import controllers.actions.CartNotEmpty;
import io.sphere.client.shop.model.*;
import forms.cartForm.AddToCart;
import forms.cartForm.UpdateCart;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import sphere.ShopController;
import views.html.carts;

import static play.data.Form.form;

public class CartController extends ShopController {

    final static Form<AddToCart> addToCartForm = form(AddToCart.class);
    final static Form<UpdateCart> updateCartForm = form(UpdateCart.class);

    @With(CartNotEmpty.class)
    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        return ok(carts.render(cart));
    }

    /**
     * Adds a product of a specified quantity to a cart.
     *
     * @return In success case the cart overview page.
     */
    public static Result add() {
        Form<AddToCart> form = addToCartForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "The item could not be added to your cart, please try again.");
            return redirect(session("returnUrl"));
        }
        // Case invalid product
        AddToCart addToCart = form.get();
        Product product = sphere().products().byId(addToCart.productId).fetch().orNull();
        if (product == null) {
            return notFound("Product not found");
        }
        // Case invalid variant
        Variant variant = product.getVariants().byId(addToCart.variantId).orNull();
        if (variant == null) {
            return notFound("Product variant not found");
        }
        // Case valid product to add to cart
        int variantId = getMatchedSizeVariant(product, variant, addToCart.size);
        sphere().currentCart().addLineItem(addToCart.productId, variantId, addToCart.quantity);
        flash("cart-success", product.getName() + " was added to your shopping cart.");
        return Results.redirect(routes.CartController.show());
    }

    public static Result update() {
        Form<UpdateCart> form = updateCartForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "The item could not be updated in your cart, please try again.");
            return Results.redirect(routes.CartController.show());
        }
        // Case valid cart update
        UpdateCart updateCart = form.get();
        CartUpdate cartUpdate = new CartUpdate()
                .setLineItemQuantity(updateCart.lineItemId, updateCart.quantity);
        sphere().currentCart().update(cartUpdate);
        flash("cart-success", "Quantity updated.");
        return Results.redirect(routes.CartController.show());
    }

    public static Result remove(String item) {
        // Case valid cart update
        sphere().currentCart().removeLineItem(item);
        flash("cart-success", "Product removed from your shopping cart.");
        return Results.redirect(routes.CartController.show());
    }

    protected static int getMatchedSizeVariant(Product product, Variant variant, String size) {
        // When size not defined return selected variant ID
        if (size == null) return variant.getId();
        // Otherwise fetch all variants
        VariantList variants = product.getVariants();
        // Filter them by selected color, if any
        if (variant.hasAttribute("color")) {
            variants = variants.byAttributes(variant.getAttribute("color"));
        }
        // And filter them by selected size, return matching variant ID
        Attribute sizeAttr = new Attribute("size", size);
        return variants.byAttributes(sizeAttr).first().or(variant).getId();
    }

}
