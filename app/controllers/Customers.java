package controllers;

import controllers.actions.Authorization;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.exceptions.InvalidPasswordException;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import io.sphere.client.shop.model.Order;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;

import java.util.List;

import static play.data.Form.form;

@With(Authorization.class)
public class Customers extends BaseController {
    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);

    public Customers(final CategoryService categoryService, final ProductService productService,
                     final CartService cartService, final CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        return ok();
    }

    public static Result update() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdateCustomer> form = updateCustomerForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            return badRequest();
        }
        // Case valid customer update
        UpdateCustomer updateCustomer = form.get();
        CustomerUpdate update = new CustomerUpdate()
                .setName(updateCustomer.getCustomerName())
                .setEmail(updateCustomer.email);
        customer = sphere().currentCustomer().update(update);
        return ok();
    }

    public static Result updatePassword() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdatePassword> form = updatePasswordForm.bindFromRequest();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            return badRequest();
        }
        // Case invalid old password
        UpdatePassword updatePassword = form.get();
        try {
            sphere().currentCustomer().changePassword(updatePassword.oldPassword, updatePassword.newPassword);
        } catch (InvalidPasswordException e) {
            return badRequest();
        }
        // Case valid password update
        return ok();
    }
}
