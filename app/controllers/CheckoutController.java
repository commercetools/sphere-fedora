package controllers;

import controllers.actions.CartNotEmpty;
import forms.checkoutForm.SetBilling;
import forms.checkoutForm.SetShipping;
import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import io.sphere.client.shop.model.PaymentState;
import io.sphere.client.shop.model.ShippingMethod;
import models.ShopCart;
import play.libs.F;
import play.mvc.Content;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import views.html.checkout;

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
        Form<SignUp> form = signUpForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Login form contains missing or invalid data");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        }
        // Case already signed up
        SignUp signUp = form.get();
        if (sphere().login(signUp.email, signUp.password)) {
            return asPromise(redirect(routes.CheckoutController.showShipping()));
        }
        // Case already registered email
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch (EmailAlreadyInUseException e) {
            flash("error", "Provided email is already in use");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        }
        // Case valid sign up
        return asPromise(redirect(routes.CheckoutController.showShipping()));
    }

    public F.Promise<Result> logIn() {
        Form<LogIn> form = logInForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Login form contains missing or invalid data");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        }
        // Case already logged in
        LogIn logIn = form.get();
        if (sphere().isLoggedIn()) {
            return ok(showPage(SHIPPING_INFORMATION_2));
        }
        // Case invalid credentials
        if (!sphere().login(logIn.email, logIn.password)) {
            flash("error", "Invalid username or password");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        }
        // Case valid log in
        return ok(showPage(SHIPPING_INFORMATION_2));
    }

    public F.Promise<Result> setShipping() {
        Form<SetShipping> form = setShippingForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Shipping information has errors");
            return badRequest(showPage(SHIPPING_INFORMATION_2));
        }
        // Case invalid shipping method
        SetShipping setShipping = form.get();
        ShippingMethod shippingMethod = sphere().shippingMethods().byId(setShipping.method).fetch().orNull();
        if (shippingMethod == null) {
            flash("error", "Shipping method is invalid");
            return badRequest(showPage(SHIPPING_INFORMATION_2));
        }
        // Case valid shipping data
        sphere().currentCart().setCustomerEmail(setShipping.email);
        sphere().currentCart().setShippingAddress(setShipping.getAddress());
        sphere().currentCart().setShippingMethod(ShippingMethod.reference(shippingMethod.getId()));
        return ok(showPage(BILLING_INFORMATION_3));
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
