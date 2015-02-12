package forms.checkoutForm;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.Customer;
import play.data.validation.Constraints;

public class SetShipping {

    @Constraints.Required(message = "Shipping method required")
    public String method;

    @Constraints.Required(message = "First name required")
    public String firstName;

    @Constraints.Required(message = "Last name required")
    public String lastName;

    @Constraints.Email(message = "Invalid value for email")
    @Constraints.Required(message = "Email required")
    public String email;

    @Constraints.Required(message = "Street address required")
    public String street;

    public String street2;

    @Constraints.Required(message = "Postal code required")
    public String postalCode;

    public String state;

    @Constraints.Required(message = "City required")
    public String city;

    @Constraints.Required(message = "Country required")
    public String country;

    public SetShipping() {
        this(Optional.<Address>absent());
    }

    public SetShipping(Address address) {
        this(Optional.fromNullable(address));

    }

    public SetShipping(Customer customer) {
        if (customer != null) {
            this.firstName = customer.getName().getFirstName();
            this.lastName = customer.getName().getLastName();
            this.email = customer.getEmail();
        }
    }

    public SetShipping(Optional<Address> addressOption) {
        if (addressOption.isPresent()) {
            final Address address = addressOption.get();
            this.firstName = address.getFirstName();
            this.lastName = address.getLastName();
            this.email = address.getEmail();
            this.street = address.getStreetName();
            this.street2 = address.getStreetNumber();
            this.postalCode = address.getPostalCode();
            this.city = address.getCity();
            this.state = address.getState();
            this.country = address.getCountry().getAlpha2();
        }
    }

    public Address getAddress() {
        Address address = new Address(getCountryCode());
        address.setFirstName(firstName);
        address.setLastName(lastName);
        address.setEmail(email);
        address.setStreetName(street);
        address.setStreetNumber(street2);
        address.setPostalCode(postalCode);
        if (!state.isEmpty()) {
            address.setState(state);
        }
        address.setCity(city);
        return address;
    }

    public CountryCode getCountryCode() {
        return CountryCode.getByCode(this.country);
    }

}
