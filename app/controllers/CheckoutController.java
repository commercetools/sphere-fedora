package controllers;

import controllers.actions.CartNotEmpty;
import forms.checkoutForm.SetBilling;
import forms.checkoutForm.SetShipping;
import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.PaymentState;
import io.sphere.client.shop.model.ShippingMethod;
import play.mvc.Content;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.checkout;

import java.util.List;

import static controllers.CheckoutController.CheckoutStages.*;
import static play.data.Form.form;

public class CheckoutController extends BaseController {

    final static Form<SetShipping> setShippingForm = form(SetShipping.class);
    final static Form<SetBilling> setBillingForm = form(SetBilling.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);
    final static Form<LogIn> logInForm = form(LogIn.class);

    static enum CheckoutStages {
        CHECKOUT_METHOD_1(1), SHIPPING_INFORMATION_2(2), BILLING_INFORMATION_3(3), ORDER_PREVIEW_4(4);
        private final int key;

        CheckoutStages(int key) {
            this.key = key;
        }
    }

    public CheckoutController(CategoryService categoryService, ProductService productService, CartService cartService, CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    @With(CartNotEmpty.class)
    public Result show() {
        if (customerService().isLoggedIn()) {
            return showShipping();
        } else {
            return showLogin();
        }
    }

    @With(CartNotEmpty.class)
    public Result showLogin() {
        return ok(showPage(CHECKOUT_METHOD_1));
    }

    @With(CartNotEmpty.class)
    public Result showShipping() {
        return ok(showPage(SHIPPING_INFORMATION_2));
    }

    @With(CartNotEmpty.class)
    public Result showBilling() {
        return ok(showPage(BILLING_INFORMATION_3));
    }

    @With(CartNotEmpty.class)
    protected Content showPage(final CheckoutStages stage) {
        final int page = stage.key;
        Cart cart = sphere().currentCart().fetch();
        List<ShippingMethod> shippingMethods = sphere().shippingMethods().query().fetch().getResults();
        Form<SetShipping> shippingForm = setShippingForm.fill(new SetShipping(cart.getShippingAddress()));
        Form<SetBilling> billingForm = setBillingForm.fill(new SetBilling(cart.getBillingAddress()));
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
        return checkout.render(data().build(), cart, cartSnapshot, shippingMethods, page);
    }

    public Result signUp() {
        Form<SignUp> form = signUpForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Login form contains missing or invalid data");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        }
        // Case already signed up
        SignUp signUp = form.get();
        if (sphere().login(signUp.email, signUp.password)) {
            return redirect(routes.CheckoutController.showShipping());
        }
        // Case already registered email
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch (EmailAlreadyInUseException e) {
            flash("error", "Provided email is already in use");
            return badRequest(showPage(CHECKOUT_METHOD_1));
        }
        // Case valid sign up
        return redirect(routes.CheckoutController.showShipping());
    }

    public Result logIn() {
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

    public Result setShipping() {
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

    public Result setBilling() {
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

    public Result submit() {
        String cartSnapshot = form().bindFromRequest().field("cartSnapshot").valueOr("");
        if (!sphere().currentCart().isSafeToCreateOrder(cartSnapshot)) {
            flash("error", "Your cart has changed, check everything is correct");
            return badRequest(showPage(ORDER_PREVIEW_4));
        }
        sphere().currentCart().createOrder(cartSnapshot, PaymentState.Pending);
        flash("success", "Your order has successfully created!");
        return redirect(routes.Application.home());
    }
}
