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

public class OrderServiceImpl implements OrderService {
    private final Sphere sphere;

    public OrderServiceImpl(final Sphere sphere) {
        this.sphere = sphere;
    }

    @Override
    public F.Promise<Optional<ShopOrder>> fetchById(final String id) {
        return sphere.orders().byId(id).fetchAsync().map(new F.Function<Optional<Order>, Optional<ShopOrder>>() {
            @Override
            public Optional<ShopOrder> apply(Optional<Order> order) throws Throwable {
                if (order.isPresent()) {
                    ShopOrder fetchedOrder = ShopOrder.of(order.get());
                    return Optional.of(fetchedOrder);
                } else {
                    return Optional.absent();
                }
            }
        });
    }

    @Override
    public F.Promise<Optional<ShopOrder>> fetchByOrderNumber(String orderNumber) {
        final String predicate = String.format("orderNumber=\"%s\"", orderNumber);
        return Async.asPlayPromise(sphere.client().orders().query().where(predicate).fetchAsync())
                .map(new F.Function<QueryResult<Order>, Optional<ShopOrder>>() {
                    @Override
                    public Optional<ShopOrder> apply(QueryResult<Order> orderQueryResult) throws Throwable {
                        List<Order> orders = orderQueryResult.getResults();
                        if (!orders.isEmpty()) {
                            ShopOrder fetchedOrder = ShopOrder.of(orders.get(0));
                            return Optional.of(fetchedOrder);
                        } else {
                            return Optional.absent();
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
                        List<ShopOrder> customOrders = new ArrayList<>();
                        for (Order order: orderQueryResult.getResults()) {
                            customOrders.add(ShopOrder.of(order));
                        }
                        return customOrders;
                    }
                });
    }

    @Override
    public F.Promise<ShopOrder> setPaymentState(final ShopOrder order, PaymentState paymentState) {
        OrderUpdate orderUpdate = new OrderUpdate().setPaymentState(paymentState);
        return sphere.orders().updateOrderAsync(order.getVersionedId(), orderUpdate)
            .map(new F.Function<SphereResult<Order>, ShopOrder>() {
                @Override
                public ShopOrder apply(SphereResult<Order> orderSphereResult) throws Throwable {
                    if (orderSphereResult.isSuccess()) {
                        return ShopOrder.of(orderSphereResult.getValue());
                    } else {
                        throw new RuntimeException(orderSphereResult.getGenericError());
                    }
                }
            });
    }

    // TODO do not use, needs Play SDK task https://github.com/commercetools/sphere-play-sdk/issues/21
    @Deprecated
    public F.Promise<ShopOrder> setSyncPaymentInfo(ShopOrder order, String transactionId) {
        Reference<Channel> reference = Reference.create("", order.getId());
        SyncInfo syncInfo = new SyncInfo(reference, transactionId);
        OrderUpdate orderUpdate = new OrderUpdate().updateSyncInfo(syncInfo);
        return sphere.orders().updateOrderAsync(order.getVersionedId(), orderUpdate)
                .map(new F.Function<SphereResult<Order>, ShopOrder>() {
                    @Override
                    public ShopOrder apply(SphereResult<Order> orderSphereResult) throws Throwable {
                        return ShopOrder.of(orderSphereResult.getValue());
                    }
                });

    }
}
