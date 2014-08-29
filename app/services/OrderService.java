package services;

import java.util.List;

import models.ShopCustomer;
import models.ShopOrder;
import play.libs.F;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.PaymentState;

/**
 * Provides an interface to communicate with the Sphere platform to manage orders.
 */
public interface OrderService {

    /**
     * Fetches the order with the provided ID.
     * @param id internal identifier of the order.
     * @return the promise of the order with this ID, or absent if it does not exists.
     */
    F.Promise<Optional<ShopOrder>> fetchById(String id);

    /**
     * Fetches the order with the provided ID.
     * @param orderNumber external identifier of the order.
     * @return the promise of the order with this order number, or absent if it does not exists.
     */
    F.Promise<Optional<ShopOrder>> fetchByOrderNumber(String orderNumber);

    /**
     * Fetches the orders belonging to the customer with the provided ID.
     * @param customer whom the desired orders belong to.
     * @return the promise of the list of orders belonging to this customer, or empty if the customer does not exist.
     */
    F.Promise<List<ShopOrder>> fetchByCustomer(ShopCustomer customer);

    /**
     * Sets the payment state to the provided order.
     * @param order the order to be updated.
     * @param paymentState the new payment state for the order.
     * @return the promise of the updated order with the provided payment state.
     */
    F.Promise<ShopOrder> setPaymentState(ShopOrder order, PaymentState paymentState);
}
