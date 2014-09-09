package services;

import java.util.Currency;
import java.util.List;

import io.sphere.client.SphereError;
import io.sphere.client.SphereResult;
import io.sphere.client.exceptions.OutOfStockException;
import io.sphere.client.exceptions.PriceChangedException;
import io.sphere.client.exceptions.SphereBackendException;
import io.sphere.client.model.CustomObject;
import io.sphere.client.model.ReferenceId;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.CreateOrderBuilder;
import io.sphere.client.shop.model.*;
import models.ShopCart;
import models.ShopOrder;

import models.ShopProduct;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.libs.F;
import sphere.Session;
import sphere.Sphere;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import sphere.util.Async;

public class CartServiceImpl implements CartService {
    private final Sphere sphere;
    private final CheckoutService checkoutService;

    public CartServiceImpl(final Sphere sphere, CheckoutService checkoutService) {
        this.sphere = sphere;
        this.checkoutService = checkoutService;
    }

    @Override
    public Optional<VersionedId> currentVersionedId() {
        VersionedId versionedId = Session.current().getCartId();
        return Optional.fromNullable(versionedId);
    }

    @Override
    public F.Promise<ShopCart> fetchCurrent() {
        return sphere.currentCart().fetchAsync().map(new F.Function<Cart, ShopCart>() {
            @Override
            public ShopCart apply(Cart cart) throws Throwable {
                return ShopCart.of(cart);
            }
        });
    }

    @Override
    public F.Promise<Optional<ShopCart>> fetchById(final String id) {
        return Async.asPlayPromise(sphere.client().carts().byId(id).fetchAsync())
                .map(new F.Function<Optional<Cart>, Optional<ShopCart>>() {
                    @Override
                    public Optional<ShopCart> apply(Optional<Cart> cart) throws Throwable {
                        if (cart.isPresent()) {
                            ShopCart fetchedCart = ShopCart.of(cart.get());
                            return Optional.of(fetchedCart);
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }

    @Override
    public F.Promise<Optional<ShopCart>> fetchByCustomer(final String customerId) {
        return Async.asPlayPromise(sphere.client().carts().forCustomer(customerId).fetchAsync())
                .map(new F.Function<Optional<Cart>, Optional<ShopCart>>() {
                    @Override
                    public Optional<ShopCart> apply(Optional<Cart> cart) throws Throwable {
                        if (cart.isPresent()) {
                            ShopCart fetchedCart = ShopCart.of(cart.get());
                            return Optional.of(fetchedCart);
                        } else {
                            return Optional.absent();
                        }
                    }
                });
    }

    @Override
    public F.Promise<ShopCart> duplicateCart(final ShopCart originCart) {
        final CartUpdate cartUpdate = originCart.cartUpdateToDuplicateCart();
        return updateCart(createCartFrom(originCart), cartUpdate)
                .flatMap(new F.Function<ShopCart, F.Promise<ShopCart>>() {
                    @Override
                    public F.Promise<ShopCart> apply(final ShopCart targetCart) throws Throwable {
                        return checkoutService.duplicateInformation(originCart.getId(), targetCart.getId())
                                .map(new F.Function<CustomObject, ShopCart>() {
                                    @Override
                                    public ShopCart apply(CustomObject customObject) throws Throwable {
                                        return ShopCart.of(targetCart.get());
                                    }
                                });
                    }
                });
    }

    @Override
    public boolean canCreateOrder(final String cartSnapshot) {
        return sphere.currentCart().isSafeToCreateOrder(cartSnapshot);
    }

    @Override
    public String createSnapshot() {
        return sphere.currentCart().createCartSnapshotId();
    }

    @Override
    public F.Promise<ShopCart> updateInvalidProducts(final ShopCart cart) {
        // TODO Restore when variants implemented
        CartUpdate cartUpdate = new CartUpdate(); //cart.cartUpdateForInvalidProducts();
        if (!cartUpdate.isEmpty()) {
            Logger.debug("Removing invalid products from cart " + cart.getId());
        }
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> addItem(final ShopCart cart, final ShopProduct product, final int quantity) {
        CartUpdate cartUpdate = new CartUpdate().addLineItem(quantity, product.getId(), product.getSelectedVariant().getId());
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> updateItem(final ShopCart cart, final String lineItemId, final int quantity) {
        CartUpdate cartUpdate = new CartUpdate().setLineItemQuantity(lineItemId, quantity);
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> removeItem(final ShopCart cart, final String lineItemId) {
        CartUpdate cartUpdate = new CartUpdate().removeLineItem(lineItemId);
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> clearItems(final ShopCart cart) {
        CartUpdate cartUpdate = new CartUpdate();
        for (LineItem item : cart.getLineItems()) {
            cartUpdate.removeLineItem(item.getId());
        }
        for (CustomLineItem customItem : cart.getCustomLineItems()) {
            cartUpdate.removeCustomLineItem(customItem.getId());
        }
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> setCountry(final ShopCart cart, final CountryCode countryCode) {
        CartUpdate cartUpdate = new CartUpdate().setCountry(countryCode);
        Optional<Address> shippingAddress = cart.getShippingAddress();
        if (shippingAddress.isPresent()) {
            final Address updatedShippingAddress = shippingAddress.get();
            updatedShippingAddress.setCountry(countryCode);
            cartUpdate.setShippingAddress(updatedShippingAddress);
        } else {
            cartUpdate.setShippingAddress(new Address(countryCode));
        }
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> changeAddresses(final ShopCart cart, final Address shippingAddress, final Address billingAddress) {
        CartUpdate cartUpdate = new CartUpdate()
            .setShippingAddress(shippingAddress)
            .setBillingAddress(billingAddress)
            .setCountry(shippingAddress.getCountry())
            .setCustomerEmail(billingAddress.getEmail());
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> setBillingAddress(final ShopCart cart, final Address billingAddress) {
        CartUpdate cartUpdate = new CartUpdate()
            .setBillingAddress(billingAddress);
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> setShippingAddress(final ShopCart cart, final Address shippingAddress) {
        CartUpdate cartUpdate = new CartUpdate()
                .setShippingAddress(shippingAddress)
                .setCustomerEmail(shippingAddress.getEmail())
                .setCountry(shippingAddress.getCountry());
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<ShopCart> changeShipping(final ShopCart cart, final ReferenceId<ShippingMethod> shippingMethod) {
        CartUpdate cartUpdate = new CartUpdate().setShippingMethod(shippingMethod);
        return updateCart(cart, cartUpdate);
    }

    @Override
    public F.Promise<Optional<ShopOrder>> createOrder(final ShopCart cart, final String cartSnapshot) {
        return checkoutService.startOrderNumber(cart.getId()).flatMap(new F.Function<String, F.Promise<Optional<ShopOrder>>>() {
            @Override
            public F.Promise<Optional<ShopOrder>> apply(String orderNumber) throws Throwable {
                try {
                    CreateOrderBuilder builder = new CreateOrderBuilder(cart.getVersionedId(), PaymentState.Paid)
                            .setCartSnapshotId(cartSnapshot)
                            .setOrderNumber(orderNumber);
                    ShopOrder order = ShopOrder.of(sphere.currentCart().createOrder(builder));
                    return F.Promise.pure(Optional.of(order));
                } catch (final PriceChangedException pce) {
                    return handleCreateOrderError(cart, pce.getLineItemIds());
                } catch (OutOfStockException ose) {
                    return handleCreateOrderError(cart, ose.getLineItemIds());
                }
            }
        });
    }

    /**
     * Removes the line items identified by the provided list of IDs from the cart.
     * @param cart the cart from which the line items are removed.
     * @param lineItemIds the list of line item IDs to remove.
     * @return the promise of the updated cart without the line items.
     */
    protected F.Promise<ShopCart> removeItems(final ShopCart cart, final List<String> lineItemIds) {
        CartUpdate cartUpdate = new CartUpdate();
        for (String itemId : lineItemIds) {
            cartUpdate.removeLineItem(itemId);
        }
        return updateCart(cart, cartUpdate);
    }

    /**
     * Creates a new empty cart with the basic information (i.e. currency, country and inventory mode) of the provided cart.
     * @param originCart the cart which basic information is copied to the new cart.
     * @return the new empty cart.
     */
    protected ShopCart createCartFrom(final ShopCart originCart) {
        Cart createdCart;
        Currency currency = originCart.getCurrency();
        Optional<CountryCode> country = originCart.getCountry();
        Cart.InventoryMode inventoryMode = originCart.getInventoryMode();
        if (country.isPresent()) {
            createdCart = sphere.client().carts().createCart(currency, country.get(), inventoryMode).execute();
        } else {
            createdCart = sphere.client().carts().createCart(currency, inventoryMode).execute();
        }
        return ShopCart.of(createdCart);
    }

    /**
     * Updates the cart with the provided update operation.
     * @param cart the cart to which the update operation is applied.
     * @param cartUpdate the update operation to apply.
     * @return the promise of the updated cart.
     */
    protected F.Promise<ShopCart> updateCart(final ShopCart cart, final CartUpdate cartUpdate) {
        if (cartUpdate.isEmpty()) {
            return F.Promise.pure(cart);
        } else {
            ShopCart activeCart = activateCart(cart);
            return Async.asPlayPromise(sphere.client().carts().updateCart(activeCart.getVersionedId(), cartUpdate).executeAsync())
                    .flatMap(new F.Function<SphereResult<Cart>, F.Promise<ShopCart>>() {
                        @Override
                        public F.Promise<ShopCart> apply(SphereResult<Cart> result) throws Throwable {
                            if (result.isSuccess()) {
                                ShopCart updatedCart = ShopCart.of(result.getValue());
                                Session.current().putCart(result.getValue());
                                return F.Promise.pure(updatedCart);
                            } else {
                                return handleUpdateError(cart, cartUpdate, result);
                            }
                        }
                    });
        }
    }

    /**
     * Handles the errors related to an update cart operation.
     * In particular, it tries to execute again the update operation when a concurrent modification error occurs.
     * @param cart the cart to which the update operation is applied.
     * @param cartUpdate the update operation to apply.
     * @param sphereResult the result of the failed update operation, with the backend related data.
     * @return the promise of the updated cart.
     */
    protected F.Promise<ShopCart> handleUpdateError(final ShopCart cart, final CartUpdate cartUpdate,
                                                      final SphereResult<Cart> sphereResult) {
        SphereBackendException exception = sphereResult.getGenericError();
        for (SphereError error : exception.getErrors()) {
            if (error instanceof SphereError.ConcurrentModification) {
                return fetchById(cart.getId()).flatMap(new F.Function<Optional<ShopCart>, F.Promise<ShopCart>>() {
                    @Override
                    public F.Promise<ShopCart> apply(Optional<ShopCart> updatedCart) throws Throwable {
                        if (updatedCart.isPresent()) {
                            return updateCart(updatedCart.get(), cartUpdate);
                        } else {
                            throw new RuntimeException("Could not fetch cart for second try update " + cart.getVersionedId());
                        }
                    }
                });
            }
        }
        return F.Promise.throwing(exception);
    }

    /**
     * Handles the errors related to a create order error, when there are invalid products in cart.
     * In particular, it removes all conflicting line items.
     * @param cart the cart to which the update operation is applied.
     * @return the promise of the order if it could be created, of absent otherwise.
     */
    protected F.Promise<Optional<ShopOrder>> handleCreateOrderError(final ShopCart cart, final List<String> lineItemIds) {
        return removeItems(cart, lineItemIds).map(new F.Function<ShopCart, Optional<ShopOrder>>() {
            @Override
            public Optional<ShopOrder> apply(ShopCart updatedCart) throws Throwable {
                String affectedLineItems = StringUtils.join(lineItemIds, ",");
                Logger.debug(String.format("Removed %s from cart %s ", affectedLineItems, cart.getVersionedId()));
                return Optional.absent();
            }
        });
    }

    /**
     * Forces the creation of a current cart.
     * While the current cart is empty, the SDK does not create it in the backend, therefore the ID is empty,
     * and no operation can be performed. If the provided cart is not active, that means it is the current cart
     * and is being kept only in the application level.
     * @param cart the cart to activate.
     * @return the active cart.
     */
    protected ShopCart activateCart(final ShopCart cart) {
        if (cart.isActive()) {
            return cart;
        } else {
            // Force creation of current cart. From the app there are not enough tools to create it manually.
            Cart updatedCart = sphere.currentCart().update(new CartUpdate());
            return ShopCart.of(updatedCart);
        }
    }
}
