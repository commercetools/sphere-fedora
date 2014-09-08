package services;

import models.ShopCustomer;
import play.libs.F;

import com.google.common.base.Optional;

import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.CustomerName;
import io.sphere.client.shop.model.CustomerToken;

/**
 * Provides an interface to communicate with the Sphere platform to manage customers.
 */
public interface CustomerService {

    /**
     * Fetches the current customer.
     * @return the promise of the current customer, or of absent when not logged in.
     * @deprecated in a controller/service architecture the service should not access the session
     */
    @Deprecated
    F.Promise<Optional<ShopCustomer>> fetchCurrent();

    /**
     * Fetches the customer with the provided ID.
     * @param id internal identifier of the customer.
     * @return the promise of the customer with this ID, or absent if it does not exist.
     */
    F.Promise<Optional<ShopCustomer>> fetchById(String id);

    /**
     * Fetches the customer with the assigned token.
     * @param token temporal identifier of the customer.
     * @return the promise of the customer with this token, or absent if it does not exist.
     */
    F.Promise<Optional<ShopCustomer>> fetchByToken(String token);

    /**
     * Fetches the customer with the corresponding email address.
     * @param email queried email address of the customer.
     * @return the promise with the customer with this email, or absent if it does not exist.
     */
    F.Promise<Optional<ShopCustomer>> fetchByEmail(String email);

    /**
     * Gets a free customer number to be used.
     * @return a free customer number.
     */
    F.Promise<String> generateFreeCustomerNumber();

    /**
     * Signs up a new customer to the system.
     * @param email that will uniquely identify the customer.
     * @param password for the customer account.
     * @param customerName of the customer.
     * @throws exceptions.DuplicateEmailException if the email address is already used
     * @return the new customer, already logged in.
     */
    F.Promise<ShopCustomer> signUp(String email, String password, CustomerName customerName);

    /**
     * Tries to log in a customer in the system.
     * @param email that uniquely identifies the customer.
     * @param password of the customer account.
     * @return the logged in customer, or absent if login was unsuccessful.
     */
    F.Promise<Optional<ShopCustomer>> login(String email, String password);

    /**
     * Logs out the current customer from the system.
     */
    void logout();

    /**
     * Checks whether the customer is logged in.
     * @return true if the customer is logged in, false otherwise.
     */
    boolean isLoggedIn();

    /**
     * Tries to create a temporary token for the customer identified with the provided email.
     * @param email that identifies the customer.
     * @return the promise of the token, or absent if the customer does not exist.
     */
    F.Promise<Optional<CustomerToken>> createCustomerToken(String email);

    /**
     * Changes the password of the customer identified by the provided ID.
     * @param versionedId the ID that identifies the customer and the expected version.
     * @param token the token that temporarily identifies the customer.
     * @param password the new desired password for the customer account.
     * @return the promise of the updated customer with the new password.
     */
    F.Promise<ShopCustomer> resetPassword(VersionedId versionedId, String token, String password);

    /**
     * Changes the password of the provided customer.
     * @param customer the customer to be updated.
     * @param oldPassword the current password of the customer.
     * @param newPassword the new desired password for the customer account.
     * @return the promise of the updated customer with the new password.
     */
    F.Promise<ShopCustomer> changePassword(ShopCustomer customer, String oldPassword, String newPassword);

    /**
     * Adds a new address to the provided customer's address book.
     * @param customer the customer to be updated.
     * @param address the new address to be added to the customer.
     * @return the promise of the updated customer with the new address.
     */
    F.Promise<ShopCustomer> addAddress(ShopCustomer customer, Address address);

    /**
     * Changes an address from the provided customer's address book.
     * @param customer the customer to be updated.
     * @param id the ID that identifies the address to be modified.
     * @param address the address data to replace the desired address.
     * @return the promise of the updated customer with the updated address.
     */
    F.Promise<ShopCustomer> changeAddress(ShopCustomer customer, String id, Address address);

    /**
     * Removes an address from the provided customer's address book.
     * @param customer the customer to be updated.
     * @param id the ID that identifies the address to be removed.
     * @return the promise of the updated customer with the removed address.
     */
    F.Promise<ShopCustomer> removeAddress(ShopCustomer customer, String id);

    /**
     * Changes the name and email of the provided customer.
     * @param customer the customer to be updated.
     * @param customerName the new name to be assigned to the customer.
     * @param email the new email of the customer.
     * @return the promise of the updated customer with the new name and email.
     */
    F.Promise<ShopCustomer> changeData(ShopCustomer customer, CustomerName customerName, String email);
}
