package controllers;

import com.google.common.base.Optional;
import controllers.actions.Authorization;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.exceptions.InvalidPasswordException;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import io.sphere.client.shop.model.Order;
import models.ShopCustomer;
import models.ShopOrder;
import play.Logger;
import play.data.Form;
import play.libs.F;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import sphere.CurrentCustomer;
import sphere.Sphere;
import views.html.customerView;

import java.util.List;

import static play.data.Form.form;
import static utils.AsyncUtils.asPromise;

@With(Authorization.class)
public class CustomerController extends BaseController {
    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);
    private final OrderService orderService;

    public CustomerController(final CategoryService categoryService, final ProductService productService,
                              final CartService cartService, final CustomerService customerService,
                              final OrderService orderService) {
        super(categoryService, productService, cartService, customerService);
        this.orderService = orderService;
    }

    public F.Promise<Result> show() {
        return customerService().fetchCurrent().flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
            @Override
            public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                if (!shopCustomerOptional.isPresent()) {
                    final CurrentCustomer currentCustomer = Sphere.getInstance().currentCustomer();//it just access the session
                    Logger.warn(String.format("Customer %s in session, but not in SPHERE.", currentCustomer));
                    return asPromise(redirectToReturnUrl());
                } else {
                    return displayCustomerPage(shopCustomerOptional.get());
                }
            }
        });
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

    private F.Promise<Result> displayCustomerPage(final ShopCustomer customer) {
        return orderService.fetchByCustomer(customer).map(new F.Function<List<ShopOrder>, Result>() {
            @Override
            public Result apply(final List<ShopOrder> shopOrders) throws Throwable {
                final Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
                return ok(customerView.render(data().build(), customer, customerForm, shopOrders));
            }
        });
    }
}
