package controllers;

import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;
import views.html.loginView;

import static play.data.Form.form;

public class LoginController extends ShopController {

    final static Form<LogIn> logInForm = form(LogIn.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);

    /**
     * Shows a form to login as customer to this shop.
     * If a session already exists, it will be destroyed.
     *
     * @return login form page
     */
    public static Result signIn() {
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        //TODO bind from request if errors present
        return ok(loginView.render(logInForm));
    }

    public static Result showSignUp() {
        return signIn();
    }

    public static Result signUp() {
        Form<SignUp> form = signUpForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            return badRequest();
        }
        // Case already signed up
        SignUp signUp = form.get();
        if (sphere().login(signUp.email, signUp.password)) {
            return redirect(routes.CustomerController.show());
        }
        // Case already registered email
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch (EmailAlreadyInUseException e) {
            return badRequest();
        }
        // Case valid sign up
        return redirect(routes.CustomerController.show());
    }

    /**
     * Handles the login form submission.
     * @return the shop homepage on success or the login form on error.
     */
    public static Result logIn() {
        Form<LogIn> filledForm = logInForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            flash("error", "Login form contains missing or invalid data.");
            return badRequest(loginView.render(filledForm));
        } else {
            LogIn logIn = filledForm.get();
            if (sphere().isLoggedIn()) {
                return ok();
            } else if (!sphere().login(logIn.email, logIn.password)) {
                flash("error", "Invalid username or password.");
                return badRequest(loginView.render(filledForm));
            } else {
                flash("success", "You are signed in.");
                return redirect(routes.Application.home());
            }
        }
    }

    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

}
