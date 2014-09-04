package models;

import java.util.List;

import com.google.common.base.Optional;

import io.sphere.client.ReferenceException;
import io.sphere.client.model.Reference;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerGroup;
import io.sphere.client.shop.model.CustomerName;
import play.Logger;

public class ShopCustomer {
    private final Customer customer;
    private CustomerType customerType = null;
    private Optional<CustomerGroup> customerGroup = null;

    // TODO Allow to configure customer types from config file
    public enum CustomerType {
        B2C(""), B2B("b2b");

        private final String key;

        CustomerType(String key) {
            this.key = key;
        }

        public String key() {
            return this.key;
        }

        public static CustomerType ofKey(final String key) {
            CustomerType customerType;
            if (key.equalsIgnoreCase(B2B.key)) {
                customerType = B2B;
            } else {
                customerType = B2C;
            }
            return customerType;
        }
    }

    ShopCustomer(final Customer customer) {
        this.customer = customer;
    }

    public static ShopCustomer of(final Customer customer) {
        return new ShopCustomer(customer);
    }

    public Reference<CustomerGroup> getCustomerGroupReference() {
        return customer.getCustomerGroup();
    }

    public Optional<CustomerGroup> getCustomerGroup() {
        if (customerGroup == null) {
            customerGroup = Optional.absent();
            if (customer.getCustomerGroup().isExpanded() && !customer.getCustomerGroup().isEmpty()) {
                try {
                    customerGroup = Optional.of(customer.getCustomerGroup().get());
                } catch (ReferenceException re) {
                    Logger.error("Could not extract customer group: " + re.getMessage());
                }
            }
        }
        return customerGroup;
    }

    public CustomerType getCustomerType() {
        if (customerType == null) {
            customerType = CustomerType.B2C;
            if (getCustomerGroup().isPresent()) {
                customerType = CustomerType.ofKey(getCustomerGroup().get().getName());
            }
        }
        return customerType;
    }

    public String getId() {
        return customer.getId();
    }

    public VersionedId getVersionedId() {
        return customer.getIdAndVersion();
    }

    public boolean isB2C() {
        return getCustomerType().equals(CustomerType.B2C);
    }

    public boolean isB2B() {
        return getCustomerType().equals(CustomerType.B2B);
    }

    public Optional<String> getCustomerNumber() {
        String customerNumber = customer.getCustomerNumber();
        if (!customerNumber.isEmpty()) {
            return Optional.of(customerNumber);
        } else {
            return Optional.absent();
        }
    }

    public CustomerName getName() {
        return customer.getName();
    }

    public String getEmail() {
        return customer.getEmail();
    }

    public Optional<Address> getAddress(String id) {
        Address address = customer.getAddressById(id);
        if (address != null) {
            return Optional.of(address);
        } else {
            return Optional.absent();
        }
    }

    public List<Address> getAddresses() {
        return customer.getAddresses();
    }

    // TODO Find better place for this method or integrate in SDK
    public static CustomerName getCustomerName(Address address) {
        return new CustomerName(address.getTitle(), address.getFirstName(), "", address.getLastName());
    }

    @Override
    public String toString() {
        return "ShopCustomer{" +
                "customer=" + customer +
                ", customerType=" + customerType +
                ", customerGroup=" + customerGroup +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopCustomer that = (ShopCustomer) o;

        if (customer != null ? !customer.equals(that.customer) : that.customer != null) return false;
        if (customerGroup != null ? !customerGroup.equals(that.customerGroup) : that.customerGroup != null)
            return false;
        if (customerType != that.customerType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = customer != null ? customer.hashCode() : 0;
        result = 31 * result + (customerType != null ? customerType.hashCode() : 0);
        result = 31 * result + (customerGroup != null ? customerGroup.hashCode() : 0);
        return result;
    }
}
