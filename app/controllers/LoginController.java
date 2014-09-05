package controllers;

import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.loginView;
import views.html.signupView;

import static play.data.Form.form;

/**
 * handles the lifecycle of the customer login and sign up.
 */
public class LoginController extends BaseController {

    /** form for the customer credentials */
    final static Form<LogIn> logInForm = form(LogIn.class);

    /** form for new customers to register */
    final static Form<SignUp> signUpForm = form(SignUp.class);

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
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        //TODO bind from request if errors present
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
    public Result handleSignUp() {
        final Form<SignUp> filledForm = signUpForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(signupView.render(data().build(), filledForm));
        } else {
            final SignUp signUp = filledForm.get();
            if (sphere().login(signUp.email, signUp.password)) {
                return redirect(routes.CustomerController.show());
            } else {
                try {
                    sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
                } catch (EmailAlreadyInUseException e) {
                    flash("error", Messages.get(lang(), "io.sphere.client.exceptions.EmailAlreadyInUseException", signUp.email));
                    return badRequest(signupView.render(data().build(), filledForm));
                }
                return redirect(routes.CustomerController.show());
            }
        }
    }

    /**
     * Handles the login form submission.
     * @return the shop homepage on success or the login form on error.
     */
    public Result handleSignIn() {
        Form<LogIn> filledForm = logInForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            flash("error", "Login form contains missing or invalid data.");
            return badRequest(loginView.render(data().build(), filledForm));
        } else {
            LogIn logIn = filledForm.get();
            if (sphere().isLoggedIn()) {
                return ok();
            } else if (!sphere().login(logIn.email, logIn.password)) {
                flash("error", "Invalid username or password.");
                return badRequest(loginView.render(data().build(), filledForm));
            } else {
                flash("success", "You are signed in.");
                return redirect(routes.Application.home());
            }
        }
    }

    /**
     * Logs out the customer.
     * @return The last returnUrl page.
     */
    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

}
