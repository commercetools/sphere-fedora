package forms.customerForm;

import io.sphere.client.shop.model.CustomerName;
import play.data.validation.Constraints;

public class SignUp {

    @Constraints.Required(message = "First name required")
    public String firstName;

    @Constraints.Required(message = "Last name required")
    public String lastName;

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid email address")
    public String email;

    @Constraints.Required(message = "Password required")
    public String password;


    public SignUp() {

    }

    public CustomerName getCustomerName() {
        return new CustomerName(this.firstName, this.lastName);
    }

}
