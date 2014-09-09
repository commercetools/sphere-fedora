package controllers;

import com.google.common.base.Optional;
import controllers.actions.CartNotEmpty;
import exceptions.DuplicateEmailException;
import exceptions.InvalidShippingMethodException;
import forms.checkoutForm.SetBilling;
import forms.checkoutForm.SetShipping;
import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.shop.model.CustomerName;
import io.sphere.client.shop.model.PaymentState;
import io.sphere.client.shop.model.ShippingMethod;
import models.ShopCart;
import models.ShopCustomer;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Content;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import views.html.checkout;
import views.html.signupView;

import java.util.List;

import static controllers.CheckoutController.CheckoutStages.*;
import static play.data.Form.form;
import static utils.AsyncUtils.asPromise;

public class CheckoutController extends BaseController {

    final static Form<SetShipping> setShippingForm = form(SetShipping.class);
    final static Form<SetBilling> setBillingForm = form(SetBilling.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);
    final static Form<LogIn> logInForm = form(LogIn.class);
    private final CheckoutService checkoutService;
    private final ShippingMethodService shippingMethodService;

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

    @With(CartNotEmpty.class)
    public F.Promise<Result> show() {
        if (customerService().isLoggedIn()) {
            return showShipping();
        } else {
            return showLogin();
        }
    }

    @With(CartNotEmpty.class)
    public F.Promise<Result> showLogin() {
        return ok(showPage(CHECKOUT_METHOD_1));
    }

    @With(CartNotEmpty.class)
    public F.Promise<Result> showShipping() {
        return ok(showPage(SHIPPING_INFORMATION_2));
    }

    @With(CartNotEmpty.class)
    public F.Promise<Result> showBilling() {
        return ok(showPage(BILLING_INFORMATION_3));
    }

    @With(CartNotEmpty.class)
    protected F.Promise<Content> showPage(final CheckoutStages stage) {
        final int page = stage.key;
        final F.Promise<ShopCart> shopCartPromise = cartService().fetchCurrent();
        final F.Promise<List<ShippingMethod>> shippingMethodsPromise = shippingMethodService.getShippingMethods();
        return shopCartPromise.zip(shippingMethodsPromise)
                .map(new F.Function<F.Tuple<ShopCart, List<ShippingMethod>>, Content>() {
                    @Override
                    public Content apply(F.Tuple<ShopCart, List<ShippingMethod>> shopCartListTuple) throws Throwable {
                        final ShopCart cart = shopCartListTuple._1;
                        final List<ShippingMethod> shippingMethods = shopCartListTuple._2;
                        Form<SetShipping> shippingForm = setShippingForm.fill(new SetShipping(cart.getShippingAddress()));
                        Form<SetBilling> billingForm = setBillingForm.fill(new SetBilling(cart.getBillingAddress()));
                        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
                        return checkout.render(data().build(), cart.get(), cartSnapshot, shippingMethods, page);
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
                            return redirect(routes.CheckoutController.showShipping());
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
                            return cartService().changeShipping(shopCart, ShippingMethod.reference(shippingMethod.getId()))
                                    .flatMap(setShippingAddressFunction(setShipping))
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

    protected F.Function<ShopCart, F.Promise<ShopCart>> setShippingAddressFunction(final SetShipping setShipping) {
        return new F.Function<ShopCart, F.Promise<ShopCart>>() {
            @Override
            public F.Promise<ShopCart> apply(ShopCart shopCart) throws Throwable {
                return cartService().setShippingAddress(shopCart, setShipping.getAddress());
            }
        };
    }

    protected ShippingMethod requireExistingShippingMethod(Optional<ShippingMethod> shippingMethodOptional, String id) {
        if (!shippingMethodOptional.isPresent()) {
            throw InvalidShippingMethodException.ofWrongId(id);
        }
        return shippingMethodOptional.get();
    }

    public F.Promise<Result> setBilling() {
        Form<SetBilling> form = setBillingForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Billing information has errors");
            return badRequest(showPage(BILLING_INFORMATION_3));
        }
        // Case valid shipping address
        SetBilling setBilling = form.get();
        if (setBilling.email != null) {
            sphere().currentCart().setCustomerEmail(setBilling.email);
        }
        sphere().currentCart().setBillingAddress(setBilling.getAddress());
        return ok(showPage(ORDER_PREVIEW_4));
    }

    public F.Promise<Result> submit() {
        String cartSnapshot = form().bindFromRequest().field("cartSnapshot").valueOr("");
        if (!sphere().currentCart().isSafeToCreateOrder(cartSnapshot)) {
            flash("error", "Your cart has changed, check everything is correct");
            return badRequest(showPage(ORDER_PREVIEW_4));
        }
        sphere().currentCart().createOrder(cartSnapshot, PaymentState.Pending);
        flash("success", "Your order has successfully created!");
        return asPromise(redirect(routes.Application.home()));
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
