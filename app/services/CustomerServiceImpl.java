package services;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import exceptions.DuplicateEmailException;
import io.sphere.client.model.CustomObject;
import io.sphere.client.shop.model.*;
import models.ShopCart;
import models.ShopCustomer;
import play.libs.F;
import sphere.Session;
import sphere.Sphere;
import sphere.util.Async;

import com.google.common.base.Optional;

import io.sphere.client.SphereError;
import io.sphere.client.SphereResult;
import io.sphere.client.exceptions.SphereBackendException;
import io.sphere.client.model.QueryResult;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.SignInResult;
import io.sphere.client.shop.SignUpBuilder;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {
    protected static final int INITIAL_CUSTOMER_NUMBER = 1000;

    protected static final String GLOBAL_CONTAINER = "globalInfo";
    protected static final String GLOBAL_CUSTOMER_NUMBER_KEY = "lastCustomerNumber";

    protected static final String CUSTOMER_GROUP_ID = "customerGroup";

    protected static final int TOKEN_EXPIRATION_TIME = 60 * 24;

    private final Sphere sphere;
    private final CartService cartService;
    private final CustomObjectService customObjectService;

    public CustomerServiceImpl(final Sphere sphere, final CartService cartService, final CustomObjectService customObjectService) {
        this.sphere = sphere;
        this.cartService = cartService;
        this.customObjectService = customObjectService;
    }

    @Override
    public F.Promise<Optional<ShopCustomer>> fetchCurrent() {
        if (sphere.isLoggedIn()) {
            return sphere.currentCustomer().fetchAsync().map(new F.Function<Customer, Optional<ShopCustomer>>() {
                @Override
                public Optional<ShopCustomer> apply(Customer customer) throws Throwable {
                    ShopCustomer fetchedCustomer = ShopCustomer.of(customer);
                    return Optional.of(fetchedCustomer);
                }
            });
        }
        return F.Promise.pure(Optional.<ShopCustomer>absent());
    }

    @Override
    public F.Promise<Optional<ShopCustomer>> fetchById(final String id) {
        return Async.asPlayPromise(sphere.client().customers().byId(id).expand(CUSTOMER_GROUP_ID).fetchAsync())
                .map(new F.Function<Optional<Customer>, Optional<ShopCustomer>>() {
                    @Override
                    public Optional<ShopCustomer> apply(Optional<Customer> customer) throws Throwable {
                        if (customer.isPresent()) {
                            ShopCustomer fetchedCustomer = ShopCustomer.of(customer.get());
                            return Optional.of(fetchedCustomer);
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }

    @Override
    public F.Promise<Optional<ShopCustomer>> fetchByToken(final String token) {
        return sphere.customers().byToken(token).expand(CUSTOMER_GROUP_ID).fetchAsync()
                .map(new F.Function<Optional<Customer>, Optional<ShopCustomer>>() {
                    @Override
                    public Optional<ShopCustomer> apply(Optional<Customer> customer) throws Throwable {
                        if (customer.isPresent()) {
                            ShopCustomer fetchedCustomer = ShopCustomer.of(customer.get());
                            return Optional.of(fetchedCustomer);
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }

    @Override
    public F.Promise<Optional<ShopCustomer>> fetchByEmail(final String email) {
        String predicate = String.format("email=\"%s\"", email);
        return Async.asPlayPromise(sphere.client().customers().query().where(predicate).expand(CUSTOMER_GROUP_ID).fetchAsync())
                .map(new F.Function<QueryResult<Customer>, Optional<ShopCustomer>>() {
                    @Override
                    public Optional<ShopCustomer> apply(QueryResult<Customer> result) throws Throwable {
                        if (result.getResults().isEmpty()) {
                            return Optional.absent();
                        } else {
                            ShopCustomer fetchedCustomer = ShopCustomer.of(result.getResults().get(0));
                            return Optional.of(fetchedCustomer);
                        }
                    }
                });
    }

    @Override
    public F.Promise<String> generateFreeCustomerNumber() {
        return getLastUsedCustomerNumber().flatMap(new F.Function<Optional<CustomObject>, F.Promise<String>>() {
            @Override
            public F.Promise<String> apply(Optional<CustomObject> customObject) throws Throwable {
                int lastCustomerNumber = INITIAL_CUSTOMER_NUMBER;
                if (customObject.isPresent()) {
                    lastCustomerNumber = customObject.get().getValue().asInt();
                }
                Optional<Integer> version = Optional.of(customObject.get().getVersion());
                return setLastUsedCustomerNumber(lastCustomerNumber + 1, version)
                        .map(new F.Function<CustomObject, String>() {
                            @Override
                            public String apply(CustomObject customObject) throws Throwable {
                                return customObject.getValue().asText();
                            }
                        });
            }
        });
    }

    @Override
    public F.Promise<ShopCustomer> signUp(final String email, final String password, final CustomerName customerName) {
        return generateFreeCustomerNumber().flatMap(new F.Function<String, F.Promise<ShopCustomer>>() {
            @Override
            public F.Promise<ShopCustomer> apply(String customerNumber) throws Throwable {
                SignUpBuilder builder = new SignUpBuilder(email, password, customerName).setCustomerNumber(customerNumber);
                return sphere.signupAsync(builder).map(new F.Function<SphereResult<SignInResult>, ShopCustomer>() {
                    @Override
                    public ShopCustomer apply(SphereResult<SignInResult> result) throws Throwable {
                        if (result.isSuccess()) {
                            return ShopCustomer.of(result.getValue().getCustomer());
                        } else {
                            final List<SphereError> errors = result.getGenericError().getErrors();
                            final boolean emailAlreadyInUse = Iterables.any(errors, new Predicate<SphereError>() {
                                @Override
                                public boolean apply(final SphereError sphereError) {
                                    return "DuplicateField".equals(sphereError.getCode());
                                }
                            });
                            if (emailAlreadyInUse) {
                                throw new DuplicateEmailException(result.getGenericError());
                            } else {
                                throw result.getGenericError();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public F.Promise<Optional<ShopCustomer>> login(final String email, final String password) {
        if (isLoggedIn()) {
            logout();
        }
        return sphere.loginAsync(email, password)
            .flatMap(new F.Function<SphereResult<SignInResult>, F.Promise<Optional<ShopCustomer>>>() {
                @Override
                public F.Promise<Optional<ShopCustomer>> apply(SphereResult<SignInResult> signInResult) throws Throwable {
                    if (signInResult.isSuccess()) {
                        ShopCustomer loggedCustomer = ShopCustomer.of(signInResult.getValue().getCustomer());
                        return F.Promise.pure(Optional.of(loggedCustomer));
                    } else {
                        return handleLoginError(signInResult, email, password);
                    }
                }
            });
    }

    @Override
    public void logout() {
        sphere.logout();
    }

    @Override
    public boolean isLoggedIn() {
        return sphere.isLoggedIn();
    }

    @Override
    public F.Promise<Optional<CustomerToken>> createCustomerToken(final String email) {
        return Async.asPlayPromise(sphere.client().customers().createPasswordResetToken(email, TOKEN_EXPIRATION_TIME).executeAsync())
                .map(new F.Function<SphereResult<CustomerToken>, Optional<CustomerToken>>() {
                    @Override
                    public Optional<CustomerToken> apply(SphereResult<CustomerToken> result) throws Throwable {
                        if (result.isSuccess()) {
                            return Optional.of(result.getValue());
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }

    @Override
    public F.Promise<ShopCustomer> resetPassword(final VersionedId versionedId, final String token, final String password) {
        return sphere.customers().resetPasswordAsync(versionedId, token, password)
                .map(new F.Function<SphereResult<Customer>, ShopCustomer>() {
                    @Override
                    public ShopCustomer apply(SphereResult<Customer> result) throws Throwable {
                        if (result.isSuccess()) {
                            return ShopCustomer.of(result.getValue());
                        } else {
                            throw new RuntimeException(result.getGenericError());
                        }
                    }
                });
    }

    @Override
    public F.Promise<ShopCustomer> changePassword(final ShopCustomer customer, final String oldPassword, final String newPassword) {
        return Async.asPlayPromise(sphere.client().customers().changePassword(customer.getVersionedId(), oldPassword, newPassword).executeAsync())
                .map(new F.Function<SphereResult<Customer>, ShopCustomer>() {
                    @Override
                    public ShopCustomer apply(SphereResult<Customer> result) throws Throwable {
                        if (result.isSuccess()) {
                            return ShopCustomer.of(result.getValue());
                        } else {
                            throw new RuntimeException(result.getGenericError());
                        }
                    }
                });
    }

    @Override
    public F.Promise<ShopCustomer> addAddress(final ShopCustomer customer, final Address address) {
        CustomerUpdate customerUpdate = new CustomerUpdate().addAddress(address);
        return updateCustomer(customer, customerUpdate);
    }

    @Override
    public F.Promise<ShopCustomer> changeAddress(final ShopCustomer customer, final String id, final Address address) {
        CustomerUpdate customerUpdate = new CustomerUpdate().changeAddress(id, address);
        return updateCustomer(customer, customerUpdate);
    }

    @Override
    public F.Promise<ShopCustomer> removeAddress(final ShopCustomer customer, final String id) {
        CustomerUpdate customerUpdate = new CustomerUpdate().removeAddress(id);
        return updateCustomer(customer, customerUpdate);
    }

    @Override
    public F.Promise<ShopCustomer> changeData(final ShopCustomer customer, final CustomerName customerName, final String email) {
        CustomerUpdate customerUpdate = new CustomerUpdate().setName(customerName).setEmail(email);
        return updateCustomer(customer, customerUpdate);

    }

    /**
     * Updates the customer with the provided update operation.
     * @param customer the customer to which the update operation is applied.
     * @param customerUpdate the update operation to apply.
     * @return the promise of the updated customer.
     */
    protected F.Promise<ShopCustomer> updateCustomer(final ShopCustomer customer, final CustomerUpdate customerUpdate) {
        if (customerUpdate.isEmpty()) {
            return F.Promise.pure(customer);
        } else {
            return Async.asPlayPromise(sphere.client().customers().update(customer.getVersionedId(), customerUpdate).executeAsync())
                    .flatMap(new F.Function<SphereResult<Customer>, F.Promise<ShopCustomer>>() {
                        @Override
                        public F.Promise<ShopCustomer> apply(SphereResult<Customer> result) throws Throwable {
                            if (result.isSuccess()) {
                                ShopCustomer updatedCustomer = ShopCustomer.of(result.getValue());
                                return F.Promise.pure(updatedCustomer);
                            } else {
                                return handleUpdateError(customer, customerUpdate, result);
                            }
                        }
                    });
        }
    }

    /**
     * Handles the errors related to an update customer operation.
     * In particular, it tries to execute again the update operation when a concurrent modification error occurs.
     * @param customer the customer to which the update operation is applied.
     * @param customerUpdate the update operation to apply.
     * @param sphereResult the result of the failed update operation, with the backend related data.
     * @return the promise of the updated customer.
     */
    protected F.Promise<ShopCustomer> handleUpdateError(final ShopCustomer customer, final CustomerUpdate customerUpdate,
                                                      final SphereResult<Customer> sphereResult) {
        SphereBackendException exception = sphereResult.getGenericError();
        for (SphereError error : exception.getErrors()) {
            if (error instanceof SphereError.ConcurrentModification) {
                return fetchById(customer.getId()).flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<ShopCustomer>>() {
                    @Override
                    public F.Promise<ShopCustomer> apply(Optional<ShopCustomer> updatedCustomer) throws Throwable {
                        if (updatedCustomer.isPresent()) {
                            return updateCustomer(updatedCustomer.get(), customerUpdate);
                        } else {
                            throw new RuntimeException("Could not fetch customer for second try update " + customer.getVersionedId());
                        }
                    }
                });
            }
        }
        return F.Promise.throwing(exception);
    }

    /**
     * Checks the error reason for the failed login and tries to recover from it if possible.
     * Besides invalid credentials, will try to fix the corrupted cart when the operation could not be performed,
     * and will try to reset the cart with a new ID to avoid issues from merging side-effects.
     * @param signInResult the result of the login operation.
     * @param email the provided email address to log in.
     * @param password the provided password to log in.
     * @return the promise of a customer if it could be recovered, or of absent if the credentials were invalid.
     */
    protected F.Promise<Optional<ShopCustomer>> handleLoginError(final SphereResult<SignInResult> signInResult,
                                                                   final String email, final String password) {
        final SphereBackendException exception = signInResult.getGenericError();
        for (SphereError error : exception.getErrors()) {
            if (error instanceof SphereError.InvalidCredentials) {
                return F.Promise.pure(Optional.<ShopCustomer>absent());
            } else if (error instanceof SphereError.InvalidOperation) {
                return fixRegisteredCart(email).zip(fixAnonymousCart())
                    .flatMap(new F.Function<F.Tuple<Optional<ShopCart>, ShopCart>, F.Promise<Optional<ShopCustomer>>>() {
                        @Override
                        public F.Promise<Optional<ShopCustomer>> apply(F.Tuple<Optional<ShopCart>, ShopCart> cartTuple) throws Throwable {
                            return resetAnonymousCart()
                                .flatMap(new F.Function<ShopCart, F.Promise<Optional<ShopCustomer>>>() {
                                    @Override
                                    public F.Promise<Optional<ShopCustomer>> apply(ShopCart customCart) throws Throwable {
                                        if (sphere.login(email, password)) {
                                            return fetchCurrent();
                                        } else {
                                            throw exception;
                                        }
                                    }
                                });
                            }
                    });
            }
        }
        return F.Promise.throwing(exception);
    }


    /**
     * Fixes the cart associated with the provided customer email, removing all invalid products.
     * @param email of the customer whose cart is requested to fix.
     * @return the promise of the fixed cart, or of an absent cart if it could not be found.
     */
    protected F.Promise<Optional<ShopCart>> fixRegisteredCart(final String email) {
        return fetchByEmail(email).flatMap(new F.Function<Optional<ShopCustomer>, F.Promise<Optional<ShopCart>>>() {
            @Override
            public F.Promise<Optional<ShopCart>> apply(Optional<ShopCustomer> customer) throws Throwable {
                if (customer.isPresent()) {
                    return cartService.fetchByCustomer(customer.get().getId())
                            .flatMap(new F.Function<Optional<ShopCart>, F.Promise<Optional<ShopCart>>>() {
                                @Override
                                public F.Promise<Optional<ShopCart>> apply(Optional<ShopCart> registeredCart) throws Throwable {
                                    if (registeredCart.isPresent()) {
                                        F.Promise<ShopCart> fixedCart = cartService.updateInvalidProducts(registeredCart.get());
                                        return fixedCart.map(new F.Function<ShopCart, Optional<ShopCart>>() {
                                            @Override
                                            public Optional<ShopCart> apply(ShopCart cart) throws Throwable {
                                                return Optional.of(cart);
                                            }
                                        });
                                    } else {
                                        return F.Promise.pure(registeredCart);
                                    }
                                }
                            });
                } else {
                    return F.Promise.pure(Optional.<ShopCart>absent());
                }
            }
        });
    }

    /**
     * Fixes the current cart, removing all invalid products.
     * @return the promise of the fixed current cart.
     */
    protected F.Promise<ShopCart> fixAnonymousCart() {
        return cartService.fetchCurrent()
            .flatMap(new F.Function<ShopCart, F.Promise<ShopCart>>() {
                @Override
                public F.Promise<ShopCart> apply(ShopCart anonymousCart) throws Throwable {
                    return cartService.updateInvalidProducts(anonymousCart);
                }
            });
    }

    /**
     * Creates a new cart from the current cart and assigns it as current cart.
     * Useful to force a change of cart ID to avoid cart merging problems.
     * @return the promise of the new current cart.
     */
    protected F.Promise<ShopCart> resetAnonymousCart() {
        return cartService.fetchCurrent()
            .flatMap(new F.Function<ShopCart, F.Promise<ShopCart>>() {
                @Override
                public F.Promise<ShopCart> apply(ShopCart anonymousCart) throws Throwable {
                    return cartService.duplicateCart(anonymousCart).map(new F.Function<ShopCart, ShopCart>() {
                        @Override
                        public ShopCart apply(ShopCart newCart) throws Throwable {
                            Session session = Session.current();
                            session.clearCart();
                            session.putCart(newCart.get());
                            return newCart;
                        }
                    });
                }
            });
    }

    /**
     * Gets the global custom object with the last used customer number.
     * @return the promise of the global custom object with the last used customer number, or absent if it does not exist.
     */
    protected F.Promise<Optional<CustomObject>> getLastUsedCustomerNumber() {
        return customObjectService.getCustomObject(GLOBAL_CONTAINER, GLOBAL_CUSTOMER_NUMBER_KEY);
    }

    /**
     * Sets the global custom object with the provided last used customer number.
     * @param customerNumber last used customer number to be set for the global custom object.
     * @param version expected version of the global custom object.
     * @return the promise of the global custom object with the last used customer number set.
     */
    protected F.Promise<CustomObject> setLastUsedCustomerNumber(int customerNumber, Optional<Integer> version) {
        return customObjectService.setCustomObject(GLOBAL_CONTAINER, GLOBAL_CUSTOMER_NUMBER_KEY, customerNumber, version);
    }
}
