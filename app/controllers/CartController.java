package controllers;

import com.google.common.base.Optional;
import forms.cartForm.AddToCart;
import forms.cartForm.UpdateCart;
import models.ShopCart;
import models.ShopProduct;
import play.data.Form;
import play.libs.F;
import play.mvc.Result;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.cartView;

import static play.data.Form.form;
import static utils.AsyncUtils.asPromise;

/**
 * This controller is responsible for handling line items in a cart.
 */
public class CartController extends BaseController {

    /** Form for adding line items to the cart. */
    private final static Form<AddToCart> addToCartForm = form(AddToCart.class);

    /** Form for updating the amount of line items in a cart. */
    private final static Form<UpdateCart> updateCartForm = form(UpdateCart.class);

    public CartController(final CategoryService categoryService, final ProductService productService,
                          final CartService cartService, final CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    /**
     * Shows a detail page of the cart.
     * @return the cart page
     */
    public F.Promise<Result> show() {
        return cartService().fetchCurrent().map(new F.Function<ShopCart, Result>() {
            @Override
            public Result apply(final ShopCart shopCart) throws Throwable {
                return ok(cartView.render(data().build(), shopCart));
            }
        });
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

    /**
     * Changes the quantity of a line item in a cart.
     *
     * @return In success case the cart detail page.
     */
    public F.Promise<Result> update() {
        final Form<UpdateCart> filledForm = updateCartForm.bindFromRequest();
        final F.Promise<Result> result;
        if (filledForm.hasErrors()) {
            flash("error", "The item could not be updated in your cart, please try again.");
            result = asPromise(redirectToCartDetailPage());
        } else {
            final UpdateCart updateCart = filledForm.get();
            result = cartService().updateItem(cart(), updateCart.lineItemId, updateCart.quantity).map(new F.Function<ShopCart, Result>() {
                @Override
                public Result apply(final ShopCart shopCart) throws Throwable {
                    flash("cart-success", "Quantity updated.");
                    return redirectToCartDetailPage();
                }
            });
        }
        return result;
    }

    /**
     * Removes a line item completely from the cart
     * @param lineItemId the id of the line item to remove
     * @return In success case the cart detail page.
     */
    public F.Promise<Result> remove(final String lineItemId) {
        return cartService().removeItem(cart(), lineItemId).map(new F.Function<ShopCart, Result>() {
            @Override
            public Result apply(final ShopCart shopCart) throws Throwable {
                flash("cart-success", "Product removed from your shopping cart.");
                return redirectToCartDetailPage();
            }
        });
    }

    private F.Promise<Result> addProductToCart(final ShopProduct shopProduct, int quantity) {
        return cartService().addItem(cart(), shopProduct, quantity).map(new F.Function<ShopCart, Result>() {
            @Override
            public Result apply(ShopCart shopCart) throws Throwable {
                flash("cart-success", shopProduct.getName(locale()) + " was added to your shopping cart.");
                return redirectToCartDetailPage();
            }
        });
    }

    private static Result redirectToCartDetailPage() {
        return redirect(controllers.routes.CartController.show());
    }
}
