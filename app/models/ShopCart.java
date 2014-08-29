package models;

import static utils.PriceUtils.customerPrice;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;

import io.sphere.client.model.Money;
import io.sphere.client.model.ReferenceId;
import io.sphere.client.model.VersionedId;
import io.sphere.client.shop.model.*;

public class ShopCart {
    private final Cart cart;

    ShopCart(final Cart cart) {
        this.cart = cart;
    }

    public static ShopCart of(final Cart cart) {
        return new ShopCart(cart);
    }

    public Cart get() {
        return cart;
    }

    public boolean isActive() {
        return !cart.getId().isEmpty();
    }

    public VersionedId getVersionedId() {
        return cart.getIdAndVersion();
    }

    public Currency getCurrency() {
        return cart.getCurrency();
    }

    public Cart.InventoryMode getInventoryMode() {
        return cart.getInventoryMode();
    }

    public Optional<CountryCode> getCountry() {
        CountryCode countryCode = cart.getCountry();
        if (countryCode != null) {
            return Optional.of(countryCode);
        } else {
            return Optional.absent();
        }
    }

    public List<LineItem> getLineItems() {
        return cart.getLineItems();
    }

    public List<CustomLineItem> getCustomLineItems() {
        return cart.getCustomLineItems();
    }

    public Optional<ShippingInfo> getShippingInfo() {
        ShippingInfo shippingInfo = cart.getShippingInfo();
        if (shippingInfo != null) {
            return Optional.of(shippingInfo);
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

    public Optional<TaxedPrice> getTaxedPrice() {
        if (cart.getTaxedPrice() != null) {
            return Optional.of(cart.getTaxedPrice());
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
        Address address = cart.getShippingAddress();
        if (address != null) {
            return Optional.of(address);
        } else {
            return Optional.absent();
        }
    }

    public Optional<Address> getBillingAddress() {
        Address address = cart.getBillingAddress();
        if (address != null) {
            return Optional.of(address);
        } else {
            return Optional.absent();
        }
    }

    public boolean isDefaultShippingAddress() {
        Optional<Address> address = getShippingAddress();
        return address.isPresent() && address.get().getStreetName().isEmpty();
    }

    public int getTotalQuantity() {
        return cart.getTotalQuantity();
    }

    public Money getTotalPrice() {
        return cart.getTotalPrice();
    }

    public String getId() {
        return getVersionedId().getId();
    }

    public boolean isEmpty() {
        return getTotalQuantity() < 1;
    }

    public boolean hasProducts() {
        return !isEmpty();
    }

    public boolean needsPayment() {
        return getTotalPrice().getAmount().doubleValue() > 0;
    }

    public Optional<LineItem> getLineItemById(String itemId) {
        for (LineItem item : getLineItems()) {
            if (item.getId().equals(itemId)) {
                return Optional.of(item);
            }
        }
        return Optional.absent();
    }

    public Optional<CustomLineItem> getCustomLineItemBySlug(String slug) {
        for (CustomLineItem lineItem : getCustomLineItems()) {
            if (lineItem.getSlug().equals(slug)) {
                return Optional.of(lineItem);
            }
        }
        return Optional.absent();
    }

    public Money getLineItemsTotalPrice(Optional<ShopCustomer> customer) {
        Money itemsTotalPrice = new Money(BigDecimal.ZERO, getCurrencyCode());
        for (LineItem item : getLineItems()) {
            itemsTotalPrice = itemsTotalPrice.plus(customerPrice(item.getTotalPrice(), item.getTaxRate(), customer));
        }
        return itemsTotalPrice;
    }

    public boolean hasTaxesCalculated() {
        return getShippingAddress().isPresent();
    }

    public Money getPrice(Optional<ShopCustomer> customer) {
        if (getTaxedPrice().isPresent()) {
            if (customer.isPresent() && customer.get().isB2B()) {
                return getTaxedPrice().get().getTotalNet();
            } else {
                return getTaxedPrice().get().getTotalGross();
            }
        } else {
            return getTotalPrice();
        }
    }

    public boolean hasSameShippingAndBillingAddress() {
        Optional<Address> shipping = getShippingAddress();
        Optional<Address> billing = getBillingAddress();
        if (shipping.isPresent() && billing.isPresent()) {
            int comparison = new AddressComparator().compare(shipping.get(), billing.get());
            return comparison == 0;
        } else {
            return false;
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

    public String getCurrencyCode() {
        return getCurrency().getCurrencyCode();
    }

    /**
     * Validates that the maximum quantity per product is not reached in the cart.
     * @param productId internal identifier of the product to be added.
     * @param variantId internal identifier of the variant to be added.
     * @param maxQuantity the maximum quantity that is allowed for the same item in the same cart.
     * @return true if the item can be added without violating the maximum requirement, false otherwise.
     */
    public boolean canAddItem(String productId, int variantId, int maxQuantity) {
        for (LineItem lineItem : getLineItems()) {
            String lineProductId = lineItem.getProductId();
            int lineVariantId = lineItem.getVariant().getId();
            boolean isSameItem = productId.equals(lineProductId) && lineVariantId == variantId;
            if (isSameItem && lineItem.getQuantity() >= maxQuantity) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all required information for processing the order is present and the data is valid.
     * @param paymentMethod selected payment method of the checkout.
     * @return true is all information is valid, false otherwise.
     */
    public boolean hasValidCheckoutInformation(Optional<String> paymentMethod) {
        return getShippingAddress().isPresent() && getBillingAddress().isPresent() && getShippingInfo().isPresent()
                && paymentMethod.isPresent();
    }

    /**
     * Creates a cart update to make a copy of the cart with the same content and information.
     * @return the cart update to duplicate this cart.
     */
    public CartUpdate cartUpdateToDuplicateCart() {
        CartUpdate cartUpdate = new CartUpdate();
        for (LineItem lineItem : getLineItems()) {
            cartUpdate.addLineItem(lineItem.getQuantity(), lineItem.getProductId(), lineItem.getVariant().getId());
        }
        for (CustomLineItem customLineItem : getCustomLineItems()) {
            ReferenceId<TaxCategory> taxCategoryReferenceId = customLineItem.getTaxCategory().toReferenceIdOrNull();
            if (taxCategoryReferenceId != null) {
                cartUpdate.addCustomLineItem(customLineItem.getName(), customLineItem.getMoney(),
                    customLineItem.getSlug(), taxCategoryReferenceId, customLineItem.getQuantity());
            }
        }
        Optional<Address> shippingAddress = getShippingAddress();
        if (shippingAddress.isPresent()) {
            cartUpdate.setShippingAddress(shippingAddress.get());
        }
        Optional<Address> billingAddress = getBillingAddress();
        if (billingAddress.isPresent()) {
            cartUpdate.setBillingAddress(billingAddress.get());
        }
        Optional<ShippingInfo> shippingInfo = getShippingInfo();
        if (shippingInfo.isPresent()) {
            ReferenceId<ShippingMethod> shippingInfoReferenceId = shippingInfo.get().getShippingMethod().toReferenceIdOrNull();
            if (shippingInfoReferenceId != null) {
                cartUpdate.setShippingMethod(shippingInfoReferenceId);
            }
        }
        return cartUpdate;
    }

    public static class AddressComparator implements Comparator<Address> {
        @Override
        public int compare(Address a1, Address a2) {
            return toString(a1).compareTo(toString(a2));
        }

        public String toString(Address a) {
            if (a == null) {
                return "";
            } else {
                return a.getTitle()
                    + a.getSalutation()
                    + a.getFirstName()
                    + a.getLastName()
                    + a.getStreetName()
                    + a.getStreetNumber()
                    + a.getAdditionalStreetInfo()
                    + a.getPostalCode()
                    + a.getCity()
                    + a.getRegion()
                    + a.getState()
                    + a.getCountry()
                    + a.getAdditionalAddressInfo()
                    + a.getCompany()
                    + a.getDepartment()
                    + a.getBuilding()
                    + a.getApartment()
                    + a.getPoBox()
                    + a.getPhone()
                    + a.getMobile()
                    + a.getEmail();
            }
        }
    }
}
