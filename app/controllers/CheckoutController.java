package controllers;

import com.google.common.base.Optional;
import controllers.actions.CartNotEmpty;
import exceptions.DuplicateEmailException;
import exceptions.InvalidShippingMethodException;
import forms.checkoutForm.SetBilling;
import forms.checkoutForm.SetShipping;
import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.model.CustomObject;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.model.CustomerName;
import io.sphere.client.shop.model.ShippingMethod;
import models.PaymentMethods;
import models.ShopCart;
import models.ShopCustomer;
import models.ShopOrder;
import play.Logger;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Content;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import views.html.checkoutView;
import views.html.signupView;

import java.util.List;

import static controllers.CheckoutController.CheckoutStages.*;
import static play.data.Form.form;
import static utils.AsyncUtils.zip;

/**
 * Handles the process of creating an order from a cart.
 * In that process no (custom) line items should be added or removed.
 */
public class CheckoutController extends BaseController {

    /** form to handle the setting of the shipping address */
    final static Form<SetShipping> setShippingForm = form(SetShipping.class);

    /** form to handle the setting of payment information */
    final static Form<SetBilling> setBillingForm = form(SetBilling.class);

    /** form to quickly register new customers */
    final static Form<SignUp> signUpForm = form(SignUp.class);

    /** form to signin existing customers */
    final static Form<LogIn> logInForm = form(LogIn.class);

    /** a flash key to check if the order preview page can be viewed */
    public static final String CAN_GO_TO_ORDER_PREVIEW = "canGoToOrderPreview";
    private final CheckoutService checkoutService;
    private final ShippingMethodService shippingMethodService;

    /**
     * The checkout process is divided into four parts. This enum provides the index number for each step.
     */
    static enum CheckoutStages {
        CHECKOUT_METHOD_1(1), SHIPPING_INFORMATION_2(2), BILLING_INFORMATION_3(3), ORDER_PREVIEW_4(4);
        private final int key;

        CheckoutStages(int key) {
            this.key = key;
        }
    }

    public CheckoutController(final CategoryService categoryService, final ProductService productService,
                              final CartService cartService, final CustomerService customerService,
                              final CheckoutService checkoutService, final ShippingMethodService shippingMethodService) {
        super(categoryService, productService, cartService, customerService);
        this.checkoutService = checkoutService;
        this.shippingMethodService = shippingMethodService;
    }

    /**
     * Displays the first manually step for the checkout.
     * If the customer is not logged in he can signup or register.
     * If the customer is logged it displys the shipping page.
     * @return first manually checkout step (signup/login or shipping)
     */
    @With(CartNotEmpty.class)
    public F.Promise<Result> show() {
        if (customerService().isLoggedIn()) {
            return showShipping();
        } else {
            return showLogin();
        }
    }

    /**
     * Shows a page to login or sign up.
     * @return page
     */
    @With(CartNotEmpty.class)
    public F.Promise<Result> showLogin() {
        return ok(showPage(CHECKOUT_METHOD_1));
    }

    /**
     * Shows a page to set the shipping address and contact information
     * @return shipping address form page
     */
    @With(CartNotEmpty.class)
    public F.Promise<Result> showShipping() {
        return ok(showPage(SHIPPING_INFORMATION_2));
    }

    /**
     * Shows a page to set the billing address and payment information
     * @return billing form page
     */
    @With(CartNotEmpty.class)
    public F.Promise<Result> showBilling() {
        return ok(showPage(BILLING_INFORMATION_3));
    }

    /**
     * Shows the products with their quantities and prices.
     * @return
     */
    @With(CartNotEmpty.class)
    public F.Promise<Result> showOrderPreview() {
        if ("true".equals(flash(CAN_GO_TO_ORDER_PREVIEW))) {
            return ok(showPage(ORDER_PREVIEW_4));
        } else {
            Logger.warn("Show order preview page direct access is not allowed.");
            return show();
        }
    }

    @With(CartNotEmpty.class)
    protected F.Promise<Content> showPage(final CheckoutStages stage) {
        final int page = stage.key;
        final F.Promise<ShopCart> shopCartPromise = cartService().fetchCurrent();
        final F.Promise<List<ShippingMethod>> shippingMethodsPromise = shippingMethodService.getShippingMethods();
        final F.Promise<String> paymentMethodPromise = paymentMethodOr(PaymentMethods.VISA.key());
        return zip(shopCartPromise, shippingMethodsPromise, paymentMethodPromise, new F.Function3<ShopCart, List<ShippingMethod>, String, Content>() {
            @Override
            public Content apply(final ShopCart cart, final List<ShippingMethod> shippingMethods, final String paymentMethod) throws Throwable {
                final String cartSnapshot = cartService().createSnapshot();
                return checkoutView.render(data().build(), cart.get(), cartSnapshot, shippingMethods, paymentMethod, page);
            }
        });
    }

    private F.Promise<String> paymentMethodOr(final String defaultValue) {
        final Optional<VersionedId> idOptional = cartService().currentVersionedId();
        F.Promise<Optional<String>> paymentMethodOptionPromise = F.Promise.pure(Optional.<String>absent());
        if (idOptional.isPresent()) {
            paymentMethodOptionPromise = checkoutService.getPaymentMethod(idOptional.get().getId());
        }
        return paymentMethodOptionPromise.map(new F.Function<Optional<String>, String>() {
            @Override
            public String apply(Optional<String> paymentMethodOption) throws Throwable {

                return paymentMethodOption.or(defaultValue);
            }
        });
    }

    public F.Promise<Result> signUp() {
        final Form<SignUp> filledForm = signUpForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            flash("error", "Login form contains missing or invalid data");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        } else if (customerService().isLoggedIn()) {
            return showShipping();
        } else {
            final SignUp signUp = filledForm.get();
            return customerService().signUp(signUp.email, signUp.password, signUp.getCustomerName())
                    .map(new F.Function<ShopCustomer, Result>() {
                        @Override
                        public Result apply(final ShopCustomer shopCustomer) throws Throwable {
                            final CustomerName name = shopCustomer.getName();
                            flash("success", Messages.get(lang(), "welcomeNewCustomer", name.getFirstName(), name.getLastName()));
                            return redirect(controllers.routes.CheckoutController.showShipping());
                        }
                    })
                    .recover(new F.Function<Throwable, Result>() {
                        @Override
                        public Result apply(Throwable throwable) throws Throwable {
                            if (throwable instanceof DuplicateEmailException) {
                                flash("error", Messages.get(lang(), "error.emailAlreadyInUse", signUp.email));
                                return badRequest(signupView.render(data().build(), filledForm));
                            } else {
                                throw throwable;
                            }
                        }
                    });
        }
    }

    public F.Promise<Result> logIn() {
        final Form<LogIn> filledForm = logInForm.bindFromRequest();
        final F.Promise<Result> result;
        if (customerService().isLoggedIn()) {
            result = ok(showPage(SHIPPING_INFORMATION_2));
        } else if (filledForm.hasErrors()) {
            flash("error", "Login form contains missing or invalid data.");
            result = badRequest(showPage(CHECKOUT_METHOD_1));
        } else {
            final LogIn logIn = filledForm.get();
            final F.Promise<Optional<ShopCustomer>> customerPromise = customerService().login(logIn.email, logIn.password);
            result = customerPromise.flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
                @Override
                public F.Promise<Result> apply(final Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                    if (shopCustomerOptional.isPresent()) {
                        return ok(showPage(SHIPPING_INFORMATION_2));
                    } else {
                        flash("error", "Invalid username or password");
                        return badRequest(showPage(CHECKOUT_METHOD_1));
                    }
                }
            });
        }
        return result;
    }

    public F.Promise<Result> handleShippingAddress() {
        final Form<SetShipping> filledForm = setShippingForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            flash("error", "Shipping information has errors.");
            return badRequest(showPage(SHIPPING_INFORMATION_2));
        } else {
            final SetShipping setShipping = filledForm.get();
            return shippingMethodService.fetchById(setShipping.method)
                    .zip(cartService().fetchCurrent())
                    .flatMap(new F.Function<F.Tuple<Optional<ShippingMethod>, ShopCart>, F.Promise<Result>>() {
                        @Override
                        public F.Promise<Result> apply(F.Tuple<Optional<ShippingMethod>, ShopCart> tuple) throws Throwable {
                            final ShippingMethod shippingMethod = requireExistingShippingMethod(tuple._1, setShipping.method);
                            final ShopCart shopCart = tuple._2;
                            return cartService().setShippingAddress(shopCart, setShipping.getAddress())
                                    .flatMap(new F.Function<ShopCart, F.Promise<ShopCart>>() {
                                        @Override
                                        public F.Promise<ShopCart> apply(ShopCart shopCart1) throws Throwable {
                                            return cartService().changeShipping(shopCart, ShippingMethod.reference(shippingMethod.getId()));
                                        }
                                    })
                                    .map(f().<ShopCart>redirect(routes.CheckoutController.showBilling()));
                        }
                    }).recover(new F.Function<Throwable, Result>() {
                        @Override
                        public Result apply(final Throwable throwable) throws Throwable {
                            if (throwable instanceof InvalidShippingMethodException) {
                                flash("error", "Shipping method is invalid");
                                return redirect(routes.CheckoutController.showShipping());
                            } else {
                                throw throwable;
                            }
                        }
                    });
        }
    }

    protected ShippingMethod requireExistingShippingMethod(Optional<ShippingMethod> shippingMethodOptional, String id) {
        if (!shippingMethodOptional.isPresent()) {
            throw InvalidShippingMethodException.ofWrongId(id);
        }
        return shippingMethodOptional.get();
    }

    public F.Promise<Result> handleBillingSettings() {
        final Form<SetBilling> filledForm = setBillingForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            flash("error", "Billing information has errors");
            return badRequest(showPage(BILLING_INFORMATION_3));
        } else {
            final SetBilling setBilling = filledForm.get();
            return cartService().fetchCurrent().flatMap(new F.Function<ShopCart, F.Promise<Result>>() {
                @Override
                public F.Promise<Result> apply(final ShopCart shopCart) throws Throwable {
                    return cartService().setBillingAddress(shopCart, setBilling.getAddress())
                            .flatMap(new F.Function<ShopCart, F.Promise<CustomObject>>() {
                                @Override
                                public F.Promise<CustomObject> apply(final ShopCart shopCart) throws Throwable {
                                    return checkoutService.setPaymentMethod(shopCart.getId(), setBilling.method);
                                }
                            })
                            .map(f().<CustomObject>redirectWithFlash(routes.CheckoutController.showOrderPreview(), CAN_GO_TO_ORDER_PREVIEW, "true"));
                }
            });
        }
    }

    public F.Promise<Result> submit() {
        final String cartSnapshot = form().bindFromRequest().field("cartSnapshot").valueOr("");
        if (!cartService().canCreateOrder(cartSnapshot)) {
            flash("error", "Your cart has changed, check everything is correct");
            return badRequest(showPage(ORDER_PREVIEW_4));
        } else {
            return cartService().fetchCurrent().flatMap(new F.Function<ShopCart, F.Promise<Result>>() {
                @Override
                public F.Promise<Result> apply(final ShopCart shopCart) throws Throwable {
                    return cartService().createOrder(shopCart, cartSnapshot)
                            .map(f().<Optional<ShopOrder>>redirectWithFlash(controllers.routes.HomeController.home(), "success", "Your order has been successfully created!"));
                }
            });
        }
    }

    private F.Promise<Result> badRequest(F.Promise<Content> contentPromise) {
        return contentPromise.map(new F.Function<Content, Result>() {
            @Override
            public Result apply(final Content content) throws Throwable {
                return badRequest(content);
            }
        });
    }

    private F.Promise<Result> ok(F.Promise<Content> contentPromise) {
        return contentPromise.map(new F.Function<Content, Result>() {
            @Override
            public Result apply(final Content content) throws Throwable {
                return ok(content);
            }
        });
    }
}
