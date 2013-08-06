package controllers;

import controllers.actions.CartNotEmpty;
import forms.addressForm.SetAddress;
import forms.checkoutForm.SetBilling;
import forms.checkoutForm.SetShipping;
import io.sphere.client.shop.model.Cart;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.checkout;

import static play.data.Form.form;

public class Checkouts extends ShopController {

    final static Form<SetShipping> setShippingForm = form(SetShipping.class);
    final static Form<SetBilling> setBillingForm = form(SetBilling.class);

    @With(CartNotEmpty.class)
    public static Result show() {
        return showPage(1);
    }

    @With(CartNotEmpty.class)
    public static Result showShipping() {
        return showPage(2);
    }

    @With(CartNotEmpty.class)
    public static Result showBilling() {
        return showPage(3);
    }

    protected static Result showPage(int page) {
        Cart cart = sphere().currentCart().fetch();
        Form<SetShipping> shippingForm = setShippingForm.fill(new SetShipping(cart.getShippingAddress()));
        Form<SetBilling> billingForm = setBillingForm.fill(new SetBilling(cart.getBillingAddress()));
        return ok(checkout.render(page));
    }

    public static Result setShipping() {
        Form<SetShipping> form = setShippingForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Shipping information has errors");
            return showPage(2);
        }
        // Case valid shipping address
        SetShipping setShipping = form.get();
        if (setShipping.email != null) {
            sphere().currentCart().setCustomerEmail(setShipping.email);
        }
        sphere().currentCart().setShippingAddress(setShipping.getAddress());
        return showPage(2);
    }

    public static Result setBilling() {
        Form<SetBilling> form = setBillingForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Billing information has errors");
            return showPage(3);
        }
        // Case valid shipping address
        SetBilling setBilling = form.get();
        if (setBilling.email != null) {
            sphere().currentCart().setCustomerEmail(setBilling.email);
        }
        sphere().currentCart().setShippingAddress(setBilling.getAddress());
        return showPage(3);
    }

    public static Result submit() {
        return ok("Order created!");
    }
}
