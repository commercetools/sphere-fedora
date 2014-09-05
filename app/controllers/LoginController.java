package controllers;

import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;

import static play.data.Form.form;

public class LoginController extends ShopController {

    final static Form<LogIn> logInForm = form(LogIn.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);



    public static Result signIn() {
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        return ok();
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

    public static Result logIn() {
        Form<LogIn> form = logInForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            flash("error", "Login form contains missing or invalid data");
            return badRequest();
        }
        // Case already logged in
        LogIn logIn = form.get();
        if (sphere().isLoggedIn()) {
            return ok();
        }
        // Case invalid credentials
        if (!sphere().login(logIn.email, logIn.password)) {
            flash("error", "Invalid username or password");
            return badRequest();
        }
        // Case valid log in
        return ok();
    }

    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

}
