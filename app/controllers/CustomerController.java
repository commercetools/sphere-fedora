package controllers;

import com.google.common.base.Optional;
import controllers.actions.Authorization;
import exceptions.PasswordNotMatchException;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import models.ShopCustomer;
import models.ShopOrder;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import sphere.CurrentCustomer;
import sphere.Sphere;
import views.html.customerView;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.data.Form.form;
import static utils.AsyncUtils.asPromise;

/**
 * Deals with authorized customers and provides the "my account" page.
 */
@With(Authorization.class)
@Singleton
public class CustomerController extends BaseController {
    /** from to update the customer name and email */
    static final Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);

    /** form to set a new customer password */
    static final Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);

    private final OrderService orderService;

    @Inject
    public CustomerController(final CategoryService categoryService, final ProductService productService,
                              final CartService cartService, final CustomerService customerService,
                              final OrderService orderService) {
        super(categoryService, productService, cartService, customerService);
        this.orderService = orderService;
    }

    /**
     * Shows the "my account" page.
     * @return a page with a form for changing the name and email, a form to change the password, the list of orders
     */
    public F.Promise<Result> show() {
        return customerService().fetchCurrent().flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
            @Override
            public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                if (!shopCustomerOptional.isPresent()) {
                    final CurrentCustomer currentCustomer = Sphere.getInstance().currentCustomer();//it just access the session
                    Logger.warn(String.format("Customer %s in session, but not in SPHERE.", currentCustomer));
                    return asPromise(redirectToReturnUrl());
                } else {
                    final Form<UpdateCustomer> filledCustomerForm = updateCustomerForm.fill(new UpdateCustomer(shopCustomerOptional.get()));
                    return displayCustomerPage(shopCustomerOptional.get(), filledCustomerForm, updatePasswordForm, OK);
                }
            }
        });
    }

    /**
     * Handles the form submission to change the customer name an email.
     * @return "my account" page
     */
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

    /**
     * Handles the password change requests from the customer.
     * @return "my account" page
     */
    public F.Promise<Result> handlePasswordUpdate() {
        final Form<UpdatePassword> filledForm = updatePasswordForm.bindFromRequest();
        final F.Promise<Optional<ShopCustomer>> customerPromise = customerService().fetchCurrent();
        final F.Promise<Result> result;
        if (filledForm.hasErrors()) {
            result = customerPromise.flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
                @Override
                public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                    return displayCustomerPage(shopCustomerOptional.get(), updateCustomerForm, filledForm, BAD_REQUEST);
                }
            });
        } else {
            result = customerPromise.flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
                @Override
                public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                    final UpdatePassword updatePassword = filledForm.get();
                    final F.Promise<ShopCustomer> promise = customerService().changePassword(shopCustomerOptional.get(), updatePassword.oldPassword, updatePassword.newPassword);
                    return redirectToCustomerPage(promise).recover(recoverFromInvalidPassword());
                }
            });
        }
        return result;
    }

    private F.Function<Throwable, Result> recoverFromInvalidPassword() {
        return new F.Function<Throwable, Result>() {
            @Override
            public Result apply(final Throwable throwable) throws Throwable {
                if (throwable instanceof PasswordNotMatchException) {
                    flash("error", Messages.get(lang(), "error.passwordNotMatch"));
                    return redirect(controllers.routes.CustomerController.show());
                } else {
                    throw throwable;
                }
            }
        };
    }

    private F.Promise<Result> displayCustomerPage(final ShopCustomer customer, final Form<UpdateCustomer> updateCustomerForm, final Form<UpdatePassword> updatePasswordForm, final int responseCode) {
        return orderService.fetchByCustomer(customer).map(new F.Function<List<ShopOrder>, Result>() {
            @Override
            public Result apply(final List<ShopOrder> shopOrders) throws Throwable {
                return status(responseCode, customerView.render(data().build(), customer, updateCustomerForm, updatePasswordForm, shopOrders));
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
                return redirect(controllers.routes.CustomerController.show());
            }
        });
    }

    private F.Promise<Result> handleBadCustomerUpdateForm(F.Promise<Optional<ShopCustomer>> customerPromise, final Form<UpdateCustomer> filledForm) {
        return customerPromise.flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Result>>() {
            @Override
            public F.Promise<Result> apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                return displayCustomerPage(shopCustomerOptional.get(), filledForm, updatePasswordForm, BAD_REQUEST);
            }
        });
    }
}
