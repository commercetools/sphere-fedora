package models;

import java.math.BigDecimal;
import java.util.List;

import io.sphere.client.shop.model.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;

import io.sphere.client.model.Money;
import io.sphere.client.model.VersionedId;

import static utils.PriceUtils.customerPrice;

public class ShopOrder {

    private final Order order;

    ShopOrder(final Order order) {
        this.order = order;
    }

    public static ShopOrder of(final Order order) {
        return new ShopOrder(order);
    }

    public String getId() {
        return order.getId();
    }

    public VersionedId getVersionedId() {
        return order.getIdAndVersion();
    }

    public String getOrderNumber() {
        return order.getOrderNumber();
    }

    public String getCustomerId() {
        return order.getCustomerId();
    }

    public String getCustomerEmail() {
        return order.getCustomerEmail();
    }

    public String getCurrencyCode() {
        return order.getCurrency().getCurrencyCode();
    }

    public boolean isEmpty() {
        return order.getTotalQuantity() < 1;
    }

    public List<LineItem> getLineItems() {
        return order.getLineItems();
    }

    public List<CustomLineItem> getCustomLineItems() {
        return order.getCustomLineItems();
    }

    public Money getLineItemsTotalPrice(Optional<ShopCustomer> customer) {
        Money itemsTotalPrice = new Money(BigDecimal.ZERO, getCurrencyCode());
        for (LineItem item : getLineItems()) {
            itemsTotalPrice = itemsTotalPrice.plus(customerPrice(item.getTotalPrice(), item.getTaxRate(), customer));
        }
        return itemsTotalPrice;
    }

    public Money getTotalPrice(Optional<ShopCustomer> customer) {
        if (getTaxedPrice().isPresent()) {
            if (customer.isPresent()) {
                if (customer.get().isB2B()) {
                    return getTaxedPrice().get().getTotalNet();
                } else {
                    return getTaxedPrice().get().getTotalGross();
                }
            }
        }
        return order.getTotalPrice();
    }

    public boolean areTaxesIncluded(Optional<ShopCustomer> customer) {
        return !customer.isPresent() || customer.get().isB2C();
    }

    public Optional<TaxedPrice> getTaxedPrice() {
        if (order.getTaxedPrice() != null) {
            return Optional.of(order.getTaxedPrice());
        } else {
            return Optional.absent();
        }
    }

    /**
     * Calculates the total tax applied. Assumes that the tax portion is constant for all products.
     * @return the total tax applied to the purchase.
     */
    public Money getTotalTax() {
        Optional<TaxedPrice> taxedPrice = getTaxedPrice();
        if (taxedPrice.isPresent()) {
            Money grossPrice = taxedPrice.get().getTotalGross();
            Money netPrice = taxedPrice.get().getTotalNet();
            return grossPrice.plus(netPrice.multiply(-1));
        } else {
            return new Money(BigDecimal.ZERO, getCurrencyCode());
        }
    }

    public Optional<Address> getShippingAddress() {
        if (order.getShippingAddress() != null) {
            return Optional.of(order.getShippingAddress());
        } else {
            return Optional.absent();
        }
    }

    public Optional<Address> getBillingAddress() {
        if (order.getBillingAddress() != null) {
            return Optional.of(order.getBillingAddress());
        } else {
            return Optional.absent();
        }
    }

    public Optional<ShippingInfo> getShippingInfo() {
        ShippingInfo shippingInfo = order.getShippingInfo();
        if (shippingInfo != null) {
            return Optional.of(shippingInfo);
        } else {
            return Optional.absent();
        }
    }

    public Optional<Money> getShippingPrice(Optional<ShopCustomer> customer) {
        Optional<ShippingInfo> shippingInfo = getShippingInfo();
        if (shippingInfo.isPresent()) {
            Money price = shippingInfo.get().getPrice();
            TaxRate taxRate = shippingInfo.get().getTaxRate();
            return Optional.of(customerPrice(price, taxRate, customer));
        } else {
            return Optional.absent();
        }
    }

    public Optional<String> getShippingMethodName() {
        Optional<ShippingInfo> shippingInfo = getShippingInfo();
        if (shippingInfo.isPresent()) {
            return Optional.of(shippingInfo.get().getShippingMethodName());
        } else {
            return Optional.absent();
        }
    }

    public ShipmentState getShippingState() {
        return order.getShipmentState();
    }

    public PaymentState getPaymentState() {
        return order.getPaymentState();
    }

    public CustomerName getCustomerName() {
        if (getShippingAddress().isPresent()) {
            return ShopCustomer.getCustomerName(getShippingAddress().get());
        } else if (getBillingAddress().isPresent()) {
            return ShopCustomer.getCustomerName(getBillingAddress().get());
        } else {
            return new CustomerName("", "");
        }
    }

    public String getOrderDate(String pattern) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
        return formatter.print(order.getCreatedAt());
    }

    public Optional<CustomLineItem> getCustomLineItemBySlug(String slug) {
        for (CustomLineItem lineItem : getCustomLineItems()) {
            if (lineItem.getSlug().equals(slug)) {
                return Optional.of(lineItem);
            }
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "ShopOrder{" +
                "order=" + order +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopOrder shopOrder = (ShopOrder) o;

        if (order != null ? !order.equals(shopOrder.order) : shopOrder.order != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return order != null ? order.hashCode() : 0;
    }
}
