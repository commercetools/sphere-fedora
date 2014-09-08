package controllers;

import com.google.common.base.Optional;
import controllers.actions.Authorization;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.exceptions.InvalidPasswordException;
import io.sphere.client.shop.model.Customer;
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
    static final Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    static final Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);
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
                    final Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(shopCustomerOptional.get()));
                    return displayCustomerPage(shopCustomerOptional.get(), customerForm, OK);
                }
            }
        });
    }

    public F.Promise<Result> handleCustomerUpdate() {
        final F.Promise<Optional<ShopCustomer>> customerPromise = customerService().fetchCurrent();
        final Form<UpdateCustomer> filledForm = updateCustomerForm.bindFromRequest();
        F.Promise<Result> result;
        if (filledForm.hasErrors()) {
            result = handleBadCustomerUpdateForm(customerPromise, filledForm);
        } else {
            result = applyCustomerUpdate(customerPromise, filledForm);
        }
        return result;
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

    private F.Promise<Result> displayCustomerPage(final ShopCustomer customer, final Form<UpdateCustomer> updateCustomerForm, final int responseCode) {
        return orderService.fetchByCustomer(customer).map(new F.Function<List<ShopOrder>, Result>() {
            @Override
            public Result apply(final List<ShopOrder> shopOrders) throws Throwable {
                return status(responseCode, customerView.render(data().build(), customer, updateCustomerForm, shopOrders));
            }
        });
    }

    private F.Promise<Result> applyCustomerUpdate(F.Promise<Optional<ShopCustomer>> customerPromise, final Form<UpdateCustomer> filledForm) {
        final UpdateCustomer updateCustomer = filledForm.get();
        return customerPromise.flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
            @Override
            public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                final F.Promise<ShopCustomer> updatedCustomerPromise = customerService().changeData(shopCustomerOptional.get(), updateCustomer.getCustomerName(), updateCustomer.email);
                return redirectToCustomerPage(updatedCustomerPromise);
            }
        });
    }

    private <T> F.Promise<Result> redirectToCustomerPage(F.Promise<T> promise) {
        return promise.map(new F.Function<T, Result>() {
            @Override
            public Result apply(final T t) throws Throwable {
                return redirect(routes.CustomerController.show());
            }
        });
    }

    private F.Promise<Result> handleBadCustomerUpdateForm(F.Promise<Optional<ShopCustomer>> customerPromise, final Form<UpdateCustomer> filledForm) {
        return customerPromise.flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
            @Override
            public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                return displayCustomerPage(shopCustomerOptional.get(), filledForm, BAD_REQUEST);
            }
        });
    }
}
