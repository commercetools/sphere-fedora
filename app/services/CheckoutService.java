package services;

import models.ShopCart;
import models.ShippingMethods;

import org.joda.time.DateTime;

import play.libs.F;

import com.google.common.base.Optional;

import io.sphere.client.model.CustomObject;

/**
 * Provides an interface to communicate with the Sphere platform to manage all data related to the checkout process.
 */
public interface CheckoutService {

    /**
     * Gets all shipping methods that are applied to the provided cart.
     * @param cart the cart to fetch the shipping methods for.
     * @return the promise of the shipping methods applied (empty when none could be applied, i.e. no shipping address).
     */
    F.Promise<ShippingMethods> getShippingMethods(ShopCart cart);

    /**
     * Copies the checkout information from the cart identified with the origin ID, to the cart with the target ID.
     * @param originCartId internal identifier of the cart or order which information wants to be copied to another cart.
     * @param targetCartId internal identifier of the cart or order which information wants to be replaced.
     * @return the promise of the custom object representing the checkout information for the target cart,
     * with the data copied from the origin cart.
     */
    F.Promise<CustomObject> duplicateInformation(String originCartId, String targetCartId);

    /**
     * Gets the order number of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @return the promise of the order number of the cart or order, or absent if it does not exist.
     */
    F.Promise<Optional<String>> getOrderNumber(String cartId);

    /**
     * Sets the order number of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @param orderNumber the order number to be set.
     * @return the promise of the updated custom object representing the checkout information with the order number.
     */
    F.Promise<CustomObject> setOrderNumber(String cartId, String orderNumber);

    /**
     * Gets the order number of the cart or order with the provided ID.
     * If the order number does not exist, a unique number is assigned to this checkout.
     * @param cartId internal identifier of the cart or order.
     * @return the promise of the order number of the cart or order.
     */
    F.Promise<String> startOrderNumber(String cartId);

    /**
     * Gets a free order number to be used.
     * @return a free order number.
     */
    F.Promise<String> generateFreeOrderNumber();

    /**
     * Gets the payment transaction ID of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @return the promise of the payment transaction ID of the cart or order, or absent if it does not exist.
     */
    F.Promise<Optional<String>> getPaymentTransaction(String cartId);

    /**
     * Sets the payment transaction ID of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @param paymentTransaction the payment transaction ID to be set.
     * @return the promise of the updated custom object representing the checkout information with the payment transaction ID.
     */
    F.Promise<CustomObject> setPaymentTransaction(String cartId, String paymentTransaction);

    /**
     * Gets the payment method of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @return the promise of the payment method of the cart or order, or absent if it does not exist.
     */
    F.Promise<Optional<String>> getPaymentMethod(String cartId);

    /**
     * Sets the payment method of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @param paymentMethod the payment method to be set.
     * @return the promise of the updated custom object representing the checkout information with the payment method.
     */
    F.Promise<CustomObject> setPaymentMethod(String cartId, String paymentMethod);

    /**
     * Gets the last payment notification timestamp of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @return the promise of the last payment notification timestamp of the cart or order, or absent if it does not exist.
     */
    F.Promise<Optional<DateTime>> getPaymentTimestamp(String cartId);

    /**
     * Sets the payment timestamp of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @param timestamp the payment timestamp to be set.
     * @return the promise of the updated custom object representing the checkout information with the payment timestamp.
     */
    F.Promise<CustomObject> setPaymentTimestamp(String cartId, DateTime timestamp);
}
