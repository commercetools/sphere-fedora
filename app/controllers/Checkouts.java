package controllers;

import controllers.actions.CartNotEmpty;
import forms.addressForm.SetAddress;
import io.sphere.client.shop.model.Cart;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.checkout;

import static play.data.Form.form;

public class Checkouts extends ShopController {

    final static Form<SetAddress> setAddressForm = form(SetAddress.class);

    @With(CartNotEmpty.class)
    public static Result show() {
        return showPage(1);
    }

    @With(CartNotEmpty.class)
    public static Result showShippingAddress() {
        return showPage(2);
    }

    @With(CartNotEmpty.class)
    public static Result showPaymentMethod() {
        return showPage(3);
    }

    protected static Result showPage(int page) {
        Cart cart = sphere().currentCart().fetch();
        Form<SetAddress> addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress()));
        return ok(checkout.render());
    }

    public static Result setShippingAddress() {
        Cart cart;
        Form<SetAddress> form = setAddressForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            cart = sphere().currentCart().fetch();
            return badRequest();
        }
        // Case valid shipping address
        SetAddress setAddress = form.get();
        if (setAddress.email != null) {
            sphere().currentCart().setCustomerEmail(setAddress.email);
        }
        cart = sphere().currentCart().setShippingAddress(setAddress.getAddress());
        return ok();
    }

    public static Result submit() {
        return ok("Order created!");
    }
}
