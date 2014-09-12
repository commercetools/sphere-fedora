package controllers;

import com.google.common.base.Optional;
import exceptions.DuplicateEmailException;
import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import models.ShopCustomer;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Result;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.loginView;
import views.html.signupView;

import javax.inject.Inject;
import javax.inject.Singleton;

import static play.data.Form.form;
import static utils.AsyncUtils.asPromise;

/**
 * handles the lifecycle of the customer login and sign up.
 */
@Singleton
public class LoginController extends BaseController {

    /** form for the customer credentials */
    final static Form<LogIn> logInForm = form(LogIn.class);

    /** form for new customers to register */
    final static Form<SignUp> signUpForm = form(SignUp.class);

    @Inject
    public LoginController(CategoryService categoryService, ProductService productService, CartService cartService, CustomerService customerService) {
        super(categoryService, productService, cartService, customerService);
    }

    /**
     * Shows a form to login as customer to this shop.
     * If a session already exists, it will be destroyed.
     *
     * @return login form page
     */
    public Result showSignIn() {
        if (customerService().isLoggedIn()) {
            customerService().logout();
        }
        return ok(loginView.render(data().build(), logInForm));
    }

    /**
     * Shows the page to become a new registered customer.
     * @return sign up form ui
     */
    public Result showSignUp() {
        return ok(signupView.render(data().build(), signUpForm));
    }

    /**
     * Handles the sign up form submission.
     *
     * @return the customer profil page or in error case the form with errors
     */
    public F.Promise<Result> handleSignUp() {
        final Form<SignUp> filledForm = signUpForm.bindFromRequest();
        if(customerService().isLoggedIn()) {
            return asPromise(redirectToReturnUrl());
        } else if (filledForm.hasErrors()) {
            return asPromise(badRequest(signupView.render(data().build(), filledForm)));
        } else {
            return handleSignUpWithValidForm(filledForm);
        }
    }

    /**
     * Handles the login form submission.
     * @return the shop homepage on success or the login form on error.
     */
    public F.Promise<Result> handleSignIn() {
        final Form<LogIn> filledForm = logInForm.bindFromRequest();
        if (customerService().isLoggedIn()) {
            return asPromise(redirect(controllers.routes.HomeController.home()));
        } else if (filledForm.hasErrors()) {
            flash("error", "Login form contains missing or invalid data.");
            return asPromise(badRequest(loginView.render(data().build(), filledForm)));
        } else {
            return handleSignInWithValidForm(filledForm);
        }
    }

    /**
     * Logs out the customer.
     * @return The last returnUrl page.
     */
    public Result logOut() {
        customerService().logout();
        return redirectToReturnUrl();
    }

    private F.Promise<Result> handleSignInWithValidForm(final Form<LogIn> filledForm) {
        final LogIn logIn = filledForm.get();
        return customerService().login(logIn.email, logIn.password).map(new F.Function<Optional<ShopCustomer>, Result>() {
            @Override
            public Result apply(Optional<ShopCustomer> shopCustomerOptional) throws Throwable {
                if (shopCustomerOptional.isPresent()) {
                    flash("success", "You are signed in.");
                    return redirect(controllers.routes.HomeController.home());
                } else {
                    flash("error", "Invalid username or password.");
                    return badRequest(loginView.render(data().build(), filledForm));
                }
            }
        });
    }

    private F.Promise<Result> handleSignUpWithValidForm(final Form<SignUp> filledForm) {
        final SignUp signUp = filledForm.get();
        return customerService().signUp(signUp.email, signUp.password, signUp.getCustomerName())
                .map(new F.Function<ShopCustomer, Result>() {
                    @Override
                    public Result apply(final ShopCustomer shopCustomer) throws Throwable {
                        return redirect(controllers.routes.CustomerController.show());
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
