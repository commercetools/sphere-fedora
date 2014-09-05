package controllers;

import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import play.data.Form;
import play.mvc.Result;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import views.html.loginView;

import static play.data.Form.form;

public class LoginController extends BaseController {

    final static Form<LogIn> logInForm = form(LogIn.class);
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
    public Result signIn() {
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        //TODO bind from request if errors present
        return ok(loginView.render(data().build(), logInForm));
    }

    public static Result showSignUp() {
        return TODO;
    }

    public static Result signUp() {
        Form<SignUp> filledForm = signUpForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest();
        } else {
            SignUp signUp = filledForm.get();
            if (sphere().login(signUp.email, signUp.password)) {
                return redirect(routes.CustomerController.show());
            } else {
                try {
                    sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
                } catch (EmailAlreadyInUseException e) {
                    return badRequest();
                }
                return redirect(routes.CustomerController.show());
            }
        }
    }

    /**
     * Handles the login form submission.
     * @return the shop homepage on success or the login form on error.
     */
    public Result logIn() {
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
