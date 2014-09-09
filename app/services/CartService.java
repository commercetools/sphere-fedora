package services;

import models.ShopCart;
import models.ShopOrder;
import models.ShopProduct;
import play.libs.F;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;

import io.sphere.client.model.ReferenceId;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.ShippingMethod;

/**
 * Provides an interface to communicate with the Sphere platform to manage carts.
 */
public interface CartService {

    /**
     * Gets the version and ID object of the current cart from the session.
     * @return the versioned ID of the current cart.
     * @deprecated service should not access the session
     */
    @Deprecated
    Optional<VersionedId> currentVersionedId();

    /**
     * Fetches the current cart.
     * @return the promise of the current cart.
     * @deprecated service should not access the session
     */
    @Deprecated
    F.Promise<ShopCart> fetchCurrent();

    /**
     * Fetches the cart with the provided ID.
     * @param id internal identifier of the cart.
     * @return the cart with this ID, or absent if it does not exist.
     */
    F.Promise<Optional<ShopCart>> fetchById(String id);

    /**
     * Fetches the cart that belongs to the provided customer.
     * @param customerId internal identifier of the customer.
     * @return the cart for this customer, or absent if it does not exist.
     */
    F.Promise<Optional<ShopCart>> fetchByCustomer(String customerId);

    /**
     * Copies the cart content and information from the origin cart to a new cart.
     * @param originCart cart which content and information wants to be copied to a new cart.
     * @return the promise of the new cart with the data copied from the origin cart.
     */
    F.Promise<ShopCart> duplicateCart(ShopCart originCart);

    /**
     * Checks whether the provided cart snapshot corresponds to the current cart.
     * @param cartSnapshot the cart snapshot of the cart.
     * @return true if the cart snapshot corresponds to the current cart, false otherwise.
     */
    boolean canCreateOrder(String cartSnapshot);

    /**
     * Creates the snapshot ID for the current cart.
     * Useful for creating an order from the cart in a secure way.
     * @return the generated snapshot ID.
     * @deprecated service should not access the session
     */
    @Deprecated
    String createSnapshot();

    /**
     * Removes all products contained in the provided cart that are outdated and updates their quantities appropriately.
     * @param cart the cart to be updated.
     * @return the promise of the cart with the updated products.
     */
    F.Promise<ShopCart> updateInvalidProducts(ShopCart cart);

    /**
     * Adds an item to the provided cart.
     * @param cart the cart to be updated.
     * @param product to be added to the cart.
     * @param quantity the amount of units to be added.
     * @return the promise of the updated cart with the provided item.
     */
    F.Promise<ShopCart> addItem(ShopCart cart, ShopProduct product, int quantity);

    /**
     * Updates the quantity of the provided line item in the cart.
     * @param cart the cart to be updated.
     * @param lineItemId to be modified in the cart.
     * @param quantity the updated amount of units.
     * @return the promise of the updated cart with the new quantity.
     */
    F.Promise<ShopCart> updateItem(ShopCart cart, String lineItemId, int quantity);

    /**
     * Removes the provided line item from the cart.
     * @param cart the cart to be updated.
     * @param lineItemId to be removed from the cart.
     * @return the promise of the updated cart without the line item.
     */
    F.Promise<ShopCart> removeItem(ShopCart cart, String lineItemId);

    /**
     * Removes all line and custom line items from the provided cart.
     * @param cart the cart to be updated.
     * @return the promise of the updated cart without line and custom line items.
     */
    F.Promise<ShopCart> clearItems(ShopCart cart);

    /**
     * Sets the provided country to the cart and to the shipping address.
     * Useful to calculate taxes and shipping costs for the desired country.
     * @param cart the cart to be updated.
     * @param countryCode the country to be set to the cart.
     * @return the promise of the updated cart with the country set.
     */
    F.Promise<ShopCart> setCountry(ShopCart cart, CountryCode countryCode);

    /**
     * Sets both shipping and billing address to the provided cart.
     * @param cart the cart to be updated.
     * @param shippingAddress the provided shipping address to be set.
     * @param billingAddress the provided billing address to be set.
     * @return the promise of the updated cart with the shipping and billing addresses.
     */
    F.Promise<ShopCart> changeAddresses(ShopCart cart, Address shippingAddress, Address billingAddress);

    /**
     * Set the shipping address for the cart.
     * @param cart the cart to be updated.
     * @param shippingAddress the provided shipping address to be set.
     * @return the promise of the updated cart
     */
    F.Promise<ShopCart> setShippingAddress(final ShopCart cart, final Address shippingAddress);

    /**
     * Sets the shipping method to the provided cart.
     * @param cart the cart to be updated.
     * @param shippingMethod the reference ID of the desired shipping method to be set.
     * @return the promise of the updated cart with the shipping method.
     */
    F.Promise<ShopCart> changeShipping(ShopCart cart, ReferenceId<ShippingMethod> shippingMethod);

    /**
     * Creates an order from the current cart and with the provided order number.
     * If any line item is not available or the price changed, the affected items are removed and the order is not created.
     * @param cart the current cart.
     * @param cartSnapshot the cart snapshot of the cart that the user requested to create an order from.
     * @throws sphere.CartModifiedException when the provided cart snapshot does not correspond to the current cart.
     * @return the promise of the created order if it could be created, absent otherwise.
     */
    F.Promise<Optional<ShopOrder>> createOrder(ShopCart cart, String cartSnapshot);
}
