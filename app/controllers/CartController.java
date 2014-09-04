package controllers;

import com.google.common.base.Optional;
import controllers.actions.CartNotEmpty;
import io.sphere.client.shop.model.*;
import forms.cartForm.AddToCart;
import forms.cartForm.UpdateCart;
import models.ShopCart;
import models.ShopProduct;
import play.data.Form;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.carts;

import static play.data.Form.form;
import static utils.AsyncUtils.asPromise;

public class CartController extends BaseController {

    final static Form<AddToCart> addToCartForm = form(AddToCart.class);
    final static Form<UpdateCart> updateCartForm = form(UpdateCart.class);

    public CartController(CategoryService categoryService, ProductService productService, CartService cartService, CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    @With(CartNotEmpty.class)
    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        return ok(carts.render(cart));
    }

    /**
     * Adds a product of a specified quantity to a cart.
     *
     * @return In success case the cart detail page.
     */
    public F.Promise<Result> add() {
        final Form<AddToCart> filledForm = addToCartForm.bindFromRequest();
        final F.Promise<Result> result;
        if (filledForm.hasErrors()) {
            flash("error", "The item could not be added to your cart, please try again.");
            result = asPromise(redirectToReturnUrl());
        } else {
            final AddToCart addToCart = filledForm.get();
            final int quantity = addToCart.quantity;
            final F.Promise<Optional<ShopProduct>> shopProductPromise = productService().fetchById(addToCart.productId, addToCart.variantId);
            result = shopProductPromise.flatMap(new F.Function<Optional<ShopProduct>, F.Promise<Result>>() {
                @Override
                public F.Promise<Result> apply(Optional<ShopProduct> shopProductOptional) throws Throwable {
                    if (!shopProductOptional.isPresent()) {
                        return asPromise(notFound("Product or product variant not found."));
                    } else {
                        return addProductToCart(shopProductOptional.get(), quantity);
                    }
                }
            });
        }
        return result;
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

    private F.Promise<Result> addProductToCart(final ShopProduct shopProduct, int quantity) {
        return cartService().addItem(cart(), shopProduct, quantity).map(new F.Function<ShopCart, Result>() {
            @Override
            public Result apply(ShopCart shopCart) throws Throwable {
                flash("cart-success", shopProduct.getName(locale()) + " was added to your shopping cart.");
                return redirect(routes.CartController.show());
            }
        });
    }
}
