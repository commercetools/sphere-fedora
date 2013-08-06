package controllers;

import controllers.actions.CartNotEmpty;
import forms.checkoutForm.SetBilling;
import forms.checkoutForm.SetShipping;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.PaymentState;
import io.sphere.client.shop.model.ShippingMethod;
import play.mvc.Content;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.checkout;

import java.util.List;

import static play.data.Form.form;

public class Checkouts extends ShopController {

    final static Form<SetShipping> setShippingForm = form(SetShipping.class);
    final static Form<SetBilling> setBillingForm = form(SetBilling.class);

    @With(CartNotEmpty.class)
    public static Result show() {
        return ok(showPage(1));
    }

    @With(CartNotEmpty.class)
    public static Result showShipping() {
        return ok(showPage(2));
    }

    @With(CartNotEmpty.class)
    public static Result showBilling() {
        return ok(showPage(3));
    }

    protected static Content showPage(int page) {
        Cart cart = sphere().currentCart().fetch();
        List<ShippingMethod> shippingMethods = sphere().shippingMethods().all().fetch().getResults();
        Form<SetShipping> shippingForm = setShippingForm.fill(new SetShipping(cart.getShippingAddress()));
        Form<SetBilling> billingForm = setBillingForm.fill(new SetBilling(cart.getBillingAddress()));
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
        return checkout.render(cart, cartSnapshot, shippingMethods, page);
    }

    public static Result setShipping() {
        Form<SetShipping> form = setShippingForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Shipping information has errors");
            return badRequest(showPage(2));
        }
        // Case invalid shipping method
        SetShipping setShipping = form.get();
        ShippingMethod shippingMethod = sphere().shippingMethods().byId(setShipping.method).fetch().orNull();
        if (shippingMethod == null) {
            flash("error", "Shipping method is invalid");
            return badRequest(showPage(2));
        }
        // Case valid shipping data
        sphere().currentCart().setCustomerEmail(setShipping.email);
        sphere().currentCart().setShippingAddress(setShipping.getAddress());
        //sphere().currentCart().setShippingMethod(shippingMethod);
        return ok(showPage(3));
    }

    public static Result setBilling() {
        Form<SetBilling> form = setBillingForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Billing information has errors");
            return badRequest(showPage(3));
        }
        // Case valid shipping address
        SetBilling setBilling = form.get();
        if (setBilling.email != null) {
            sphere().currentCart().setCustomerEmail(setBilling.email);
        }
        sphere().currentCart().setBillingAddress(setBilling.getAddress());
        return ok(showPage(4));
    }

    public static Result submit() {
        String cartSnapshot = form().bindFromRequest().field("cartSnapshot").valueOr("");
        System.out.println("CART!" + cartSnapshot);
        if (!sphere().currentCart().isSafeToCreateOrder(cartSnapshot)) {
            flash("error", "Your cart has changed, check everything is correct");
            return badRequest(showPage(4));
        }
        sphere().currentCart().createOrder(cartSnapshot, PaymentState.Pending);
        flash("success", "Your order has successfully created!");
        return redirect(routes.Application.home());
    }
}
