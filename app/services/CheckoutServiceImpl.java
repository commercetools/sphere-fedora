package services;

import static utils.JsonUtils.convertToNewFormat;
import static utils.JsonUtils.convertToOldFormat;
import static utils.JsonUtils.objectNode;

import com.google.common.base.Function;
import io.sphere.client.shop.model.ShippingMethod;
import models.ShopCart;
import models.ShippingMethods;

import org.joda.time.DateTime;

import play.libs.F;
import play.libs.Json;
import sphere.Sphere;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;

import io.sphere.client.model.CustomObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class CheckoutServiceImpl implements CheckoutService {
    protected static final int INITIAL_ORDER_NUMBER = 10000;

    protected static final String GLOBAL_CONTAINER = "globalInfo";
    protected static final String GLOBAL_ORDER_NUMBER_KEY = "lastOrderNumber";

    protected static final String CHECKOUT_CONTAINER = "checkoutInfo";
    protected static final String CHECKOUT_PAYMENT_METHOD_KEY = "paymentMethod";
    protected static final String CHECKOUT_PAYMENT_TOKEN_KEY = "paymentToken";
    protected static final String CHECKOUT_PAYMENT_TRANSACTION_KEY = "paymentTransaction";
    protected static final String CHECKOUT_PAYMENT_TIMESTAMP_KEY = "paymentTimestamp";
    protected static final String CHECKOUT_ORDER_NUMBER_KEY = "orderNumber";

    protected final Sphere sphere;
    private final CustomObjectService customObjectService;

    @Inject
    public CheckoutServiceImpl(final Sphere sphere, final CustomObjectService customObjectService) {
        this.sphere = sphere;
        this.customObjectService = customObjectService;
    }

    @Override
    public F.Promise<ShippingMethods> getShippingMethods(ShopCart cart) {
        if (cart.getShippingAddress().isPresent()) {
            return sphere.shippingMethods().forCart(cart.getId()).fetchAsync()
                    .map(new F.Function<Optional<List<ShippingMethod>>, ShippingMethods>() {
                        @Override
                        public ShippingMethods apply(Optional<List<ShippingMethod>> shippingMethods) throws Throwable {
                            return ShippingMethods.of(shippingMethods);
                        }
                    });
        }
        return F.Promise.pure(ShippingMethods.empty());
    }

    @Override
    public F.Promise<CustomObject> duplicateInformation(final String originCartId, final String targetCartId) {
        return getCheckoutInformation(originCartId).flatMap(new F.Function<Optional<CustomObject>, F.Promise<CustomObject>>() {
            @Override
            public F.Promise<CustomObject> apply(Optional<CustomObject> customObject) throws Throwable {
                ObjectNode info;
                if (customObject.isPresent()) {
                    info = objectNode(customObject.get().getValue());
                } else {
                    info = Json.newObject();
                }
                return setCheckoutInformation(targetCartId, info);
            }
        });
    }

    @Override
    public F.Promise<Optional<String>> getOrderNumber(final String cartId) {
        return getCheckoutInformation(cartId).map(new F.Function<Optional<CustomObject>, Optional<String>>() {
            @Override
            public Optional<String> apply(Optional<CustomObject> customObject) throws Throwable {
                if (customObject.isPresent()) {
                    JsonNode checkoutInfo = convertToNewFormat(customObject.get().getValue());
                    JsonNode orderNumberNode = checkoutInfo.path(CHECKOUT_ORDER_NUMBER_KEY);
                    if (!orderNumberNode.isMissingNode()) {
                        return Optional.of(orderNumberNode.asText());
                    }
                }
                return Optional.absent();
            }
        });
    }

    @Override
    public F.Promise<CustomObject> setOrderNumber(final String cartId, final String orderNumber) {
        ObjectNode json = Json.newObject();
        json.put(CHECKOUT_ORDER_NUMBER_KEY, orderNumber);
        return setCheckoutInformation(cartId, json);
    }

    @Override
    public F.Promise<String> startOrderNumber(final String cartId) {
        return getOrderNumber(cartId).flatMap(new F.Function<Optional<String>, F.Promise<String>>() {
            @Override
            public F.Promise<String> apply(Optional<String> orderNumber) throws Throwable {
                if (orderNumber.isPresent()) {
                    return F.Promise.pure(orderNumber.get());
                } else {
                    return generateFreeOrderNumber().flatMap(new F.Function<String, F.Promise<String>>() {
                        @Override
                        public F.Promise<String> apply(String uniqueOrderNumber) throws Throwable {
                            return setOrderNumber(cartId, uniqueOrderNumber).map(new F.Function<CustomObject, String>() {
                                @Override
                                public String apply(CustomObject customObject) throws Throwable {
                                    return customObject.getValue().path(CHECKOUT_ORDER_NUMBER_KEY).asText();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    public F.Promise<String> generateFreeOrderNumber() {
        return getLastUsedOrderNumber().flatMap(new F.Function<Optional<CustomObject>, F.Promise<String>>() {
            @Override
            public F.Promise<String> apply(Optional<CustomObject> customObject) throws Throwable {
                int lastOrderNumber = INITIAL_ORDER_NUMBER;
                if (customObject.isPresent()) {
                    lastOrderNumber = customObject.get().getValue().asInt();
                }
                final Optional<Integer> customObjectVersion = customObject.transform(new Function<CustomObject, Integer>() {
                    @Override
                    public Integer apply(final CustomObject customObject) {
                        return customObject.getVersion();
                    }
                });
                return setLastUsedOrderNumber(lastOrderNumber + 1, customObjectVersion)
                        .map(new F.Function<CustomObject, String>() {
                            @Override
                            public String apply(final CustomObject customObject) throws Throwable {
                                return customObject.getValue().asText();
                            }
                        });
            }
        });
    }

    @Override
    public F.Promise<Optional<String>> getPaymentTransaction(final String cartId) {
        return getCheckoutInformation(cartId).map(new F.Function<Optional<CustomObject>, Optional<String>>() {
            @Override
            public Optional<String> apply(Optional<CustomObject> customObject) throws Throwable {
                if (customObject.isPresent()) {
                    JsonNode checkoutInfo = convertToNewFormat(customObject.get().getValue());
                    JsonNode paymentTransactionNode = checkoutInfo.path(CHECKOUT_PAYMENT_TRANSACTION_KEY);
                    if (!paymentTransactionNode.isMissingNode()) {
                        return Optional.of(paymentTransactionNode.asText());
                    }
                }
                return Optional.absent();
            }
        });
    }

    @Override
    public F.Promise<CustomObject> setPaymentTransaction(final String cartId, final String paymentTransaction) {
        ObjectNode node = Json.newObject();
        node.put(CHECKOUT_PAYMENT_TRANSACTION_KEY, paymentTransaction);
        return setCheckoutInformation(cartId, node);
    }

    @Override
    public F.Promise<Optional<String>> getPaymentMethod(final String cartId) {
        return getCheckoutInformation(cartId).map(new F.Function<Optional<CustomObject>, Optional<String>>() {
            @Override
            public Optional<String> apply(Optional<CustomObject> customObject) throws Throwable {
                if (customObject.isPresent()) {
                    JsonNode checkoutInfo = convertToNewFormat(customObject.get().getValue());
                    JsonNode paymentMethodNode = checkoutInfo.path(CHECKOUT_PAYMENT_METHOD_KEY);
                    if (!paymentMethodNode.isMissingNode()) {
                        return Optional.of(paymentMethodNode.asText());
                    }
                }
                return Optional.absent();
            }
        });

    }

    @Override
    public F.Promise<Optional<String>> getPaymentToken(final String cartId) {
        return getCheckoutInformation(cartId).map(new F.Function<Optional<CustomObject>, Optional<String>>() {
            @Override
            public Optional<String> apply(Optional<CustomObject> customObject) throws Throwable {
                if (customObject.isPresent()) {
                    JsonNode checkoutInfo = convertToNewFormat(customObject.get().getValue());
                    JsonNode paymentTokenNode = checkoutInfo.path(CHECKOUT_PAYMENT_TOKEN_KEY);
                    if (!paymentTokenNode.isMissingNode()) {
                        return Optional.of(paymentTokenNode.asText());
                    }
                }
                return Optional.absent();
            }
        });

    }

    @Override
    public F.Promise<CustomObject> setPaymentMethod(final String cartId, final String paymentMethod, String token) {
        ObjectNode json = Json.newObject();
        json.put(CHECKOUT_PAYMENT_METHOD_KEY, paymentMethod);
        json.put(CHECKOUT_PAYMENT_TOKEN_KEY, token);
        return setCheckoutInformation(cartId, json);
    }

    @Override
    public F.Promise<Optional<DateTime>> getPaymentTimestamp(final String cartId) {
        return getCheckoutInformation(cartId).map(new F.Function<Optional<CustomObject>, Optional<DateTime>>() {
            @Override
            public Optional<DateTime> apply(Optional<CustomObject> customObject) throws Throwable {
                if (customObject.isPresent()) {
                    JsonNode checkoutInfo = convertToNewFormat(customObject.get().getValue());
                    JsonNode timestampNode = checkoutInfo.path(CHECKOUT_PAYMENT_TIMESTAMP_KEY);
                    if (!timestampNode.isMissingNode()) {
                        DateTime timestamp = DateTime.parse(timestampNode.asText());
                        return Optional.of(timestamp);
                    }
                }
                return Optional.absent();
            }
        });
    }

    @Override
    public F.Promise<CustomObject> setPaymentTimestamp(final String cartId, final DateTime timestamp) {
        ObjectNode json = Json.newObject();
        json.put(CHECKOUT_PAYMENT_TIMESTAMP_KEY, timestamp.toString());
        return setCheckoutInformation(cartId, json);
    }

    /**
     * Fetches the checkout information of the cart or order with the provided ID.
     * @param cartId internal identifier of the cart or order.
     * @return the custom object representing the checkout information, or absent if it does not exist.
     */
    protected F.Promise<Optional<CustomObject>> getCheckoutInformation(final String cartId) {
        return customObjectService.getCustomObject(CHECKOUT_CONTAINER, cartId);
    }

    /**
     * Updates the checkout information of the cart or order with the provided ID.
     * The method will only replace the data provided with the object node.
     * @param cartId internal identifier of the cart or order.
     * @param node the object node that contains the updated information for the checkout.
     * @return the promise of the custom object that contains the updated checkout information.
     */
    protected F.Promise<CustomObject> setCheckoutInformation(final String cartId, final ObjectNode node) {
        return getCheckoutInformation(cartId).flatMap(new F.Function<Optional<CustomObject>, F.Promise<CustomObject>>() {
            @Override
            public F.Promise<CustomObject> apply(Optional<CustomObject> customObject) throws Throwable {
                final ObjectNode info;
                if (customObject.isPresent()) {
                    info = objectNode(customObject.get().getValue());
                } else {
                    info = Json.newObject();
                }
                info.putAll(node);
                Optional<Integer> version = Optional.absent();
                return customObjectService.setCustomObject(CHECKOUT_CONTAINER, cartId, convertToOldFormat(node), version);
            }
        });
    }

    /**
     * Gets the global custom object with the last used order number.
     * @return the promise of the global custom object with the last used order number, or absent if it does not exist.
     */
    protected F.Promise<Optional<CustomObject>> getLastUsedOrderNumber() {
        return customObjectService.getCustomObject(GLOBAL_CONTAINER, GLOBAL_ORDER_NUMBER_KEY);
    }

    /**
     * Sets the global custom object with the provided last used order number.
     * @param orderNumber last used order number to be set for the global custom object.
     * @param version expected version of the global custom object.
     * @return the promise of the global custom object with the last used order number set.
     */
    protected F.Promise<CustomObject> setLastUsedOrderNumber(int orderNumber, Optional<Integer> version) {
        return customObjectService.setCustomObject(GLOBAL_CONTAINER, GLOBAL_ORDER_NUMBER_KEY, orderNumber, version);
    }
}
