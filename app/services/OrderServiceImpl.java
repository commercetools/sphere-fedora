package services;

import java.util.ArrayList;
import java.util.List;

import io.sphere.client.model.QueryResult;
import models.ShopCustomer;
import models.ShopOrder;
import play.libs.F;
import sphere.Sphere;

import com.google.common.base.Optional;

import io.sphere.client.SphereResult;
import io.sphere.client.model.Reference;
import io.sphere.client.shop.model.*;
import sphere.util.Async;
import utils.AsyncUtils;

import static utils.AsyncUtils.asPromise;

public class OrderServiceImpl implements OrderService {
    private final Sphere sphere;
    private final CheckoutService checkoutService;

    public OrderServiceImpl(final Sphere sphere, final CheckoutService checkoutService) {
        this.sphere = sphere;
        this.checkoutService = checkoutService;
    }

    @Override
    public F.Promise<Optional<ShopOrder>> fetchById(final String id) {
        return sphere.orders().byId(id).fetchAsync().flatMap(new F.Function<Optional<Order>, F.Promise<Optional<ShopOrder>>>() {
            @Override
            public F.Promise<Optional<ShopOrder>> apply(Optional<Order> order) throws Throwable {
                if (order.isPresent()) {
                    return createShopOrder(order.get());
                } else {
                    return asPromise(Optional.<ShopOrder>absent());
                }
            }
        });
    }

    @Override
    public F.Promise<Optional<ShopOrder>> fetchByOrderNumber(String orderNumber) {
        final String predicate = String.format("orderNumber=\"%s\"", orderNumber);
        return Async.asPlayPromise(sphere.client().orders().query().where(predicate).fetchAsync())
                .flatMap(new F.Function<QueryResult<Order>, F.Promise<Optional<ShopOrder>>>() {
                    @Override
                    public F.Promise<Optional<ShopOrder>> apply(QueryResult<Order> orderQueryResult) throws Throwable {
                        List<Order> orders = orderQueryResult.getResults();
                        if (!orders.isEmpty()) {
                            return createShopOrder(orders.get(0));
                        } else {
                            return asPromise(Optional.<ShopOrder>absent());
                        }
                    }
                });
    }

    @Override
    public F.Promise<List<ShopOrder>> fetchByCustomer(final ShopCustomer customer) {
        // TODO Order pagination
        return Async.asPlayPromise(sphere.client().orders().forCustomer(customer.getId()).pageSize(500).fetchAsync())
                .map(new F.Function<QueryResult<Order>, List<ShopOrder>>() {
                    @Override
                    public List<ShopOrder> apply(QueryResult<Order> orderQueryResult) throws Throwable {
                        List<ShopOrder> customOrders = new ArrayList<ShopOrder>();
                        for (Order order: orderQueryResult.getResults()) {
                            // TODO Use async calls
                            Optional<ShopOrder> fetchedOrder = createShopOrder(order).get(AsyncUtils.defaultTimeout());
                            if (fetchedOrder.isPresent()) {
                                customOrders.add(fetchedOrder.get());
                            }
                        }
                        return customOrders;
                    }
                });
    }

    /**
     * Creates a shop order with all related information from external sources, i.e. the payment method used.
     * @param order the order to use.
     * @return the promise of the shop order with the related information, or absent if the required information is not available.
     */
    private F.Promise<Optional<ShopOrder>> createShopOrder(final Order order) {
        return checkoutService.getPaymentMethod(order.getId())
                .map(new F.Function<Optional<String>, Optional<ShopOrder>>() {
                    @Override
                    public Optional<ShopOrder> apply(Optional<String> paymentMethod) throws Throwable {
                        ShopOrder shopOrder = ShopOrder.of(order, paymentMethod);
                        return Optional.of(shopOrder);
                    }
                });
    }
}
