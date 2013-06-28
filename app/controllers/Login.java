package controllers;

import forms.customerForm.LogIn;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import io.sphere.client.shop.model.Customer;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;

import static play.data.Form.form;

public class Login extends ShopController {

    final static Form<LogIn> logInForm = form(LogIn.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);


    public static Result show() {
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        return ok();
    }

    public static Result showSignUp() {
        return show();
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
            return redirect(routes.Customers.show());
        }
        // Case already registered email
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch (EmailAlreadyInUseException e) {
            return badRequest();
        }
        // Case valid sign up
        return redirect(routes.Customers.show());
    }

    public static Result logIn() {
        Form<LogIn> form = logInForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            return badRequest();
        }
        // Case already logged in
        LogIn logIn = form.get();
        if (sphere().isLoggedIn()) {
            Customer customer = sphere().currentCustomer().fetch();
            return redirect(routes.Customers.show());
        }
        // Case invalid credentials
        if (!sphere().login(logIn.email, logIn.password)) {
            return badRequest();
        }
        // Case valid log in
        Customer customer = sphere().currentCustomer().fetch();
        return redirect(routes.Customers.show());
    }

    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

}
