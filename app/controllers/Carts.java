package controllers;

import controllers.actions.CartNotEmpty;
import io.sphere.client.shop.model.*;
import forms.cartForm.AddToCart;
import forms.cartForm.RemoveFromCart;
import forms.cartForm.UpdateCart;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;

import static play.data.Form.form;

public class Carts extends ShopController {

    final static Form<AddToCart> addToCartForm = form(AddToCart.class);
    final static Form<UpdateCart> updateCartForm = form(UpdateCart.class);
    final static Form<RemoveFromCart> removeFromCartForm = form(RemoveFromCart.class);

    @With(CartNotEmpty.class)
    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        return ok();
    }


    public static Result add() {
        Form<AddToCart> form = addToCartForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            return badRequest(); // TODO Decide where to return to
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
        Cart cart = sphere().currentCart().addLineItem(addToCart.productId, variantId, addToCart.quantity);
        return ok();
    }

    public static Result update() {
        Form<UpdateCart> form = updateCartForm.bindFromRequest();
        Cart cart;
        // Case missing or invalid form data
        if (form.hasErrors()) {
            cart = sphere().currentCart().fetch();
            return badRequest();
        }
        // Case valid cart update
        UpdateCart updateCart = form.get();
        CartUpdate cartUpdate = new CartUpdate()
                .setLineItemQuantity(updateCart.lineItemId, updateCart.quantity);
        cart = sphere().currentCart().update(cartUpdate);
        return ok();
    }

    public static Result remove() {
        Form<RemoveFromCart> form = removeFromCartForm.bindFromRequest();
        Cart cart;
        // Case missing or invalid form data
        if (form.hasErrors()) {
            cart = sphere().currentCart().fetch();
            return badRequest();
        }
        // Case valid cart update
        RemoveFromCart removeFromCart = form.get();
        cart = sphere().currentCart().removeLineItem(removeFromCart.lineItemId);
        return ok();
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
