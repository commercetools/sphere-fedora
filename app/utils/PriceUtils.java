package utils;

import com.google.common.base.Optional;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.TaxRate;
import models.ShopCustomer;

public final class PriceUtils {

    private PriceUtils() {
    }

    /**
     * Gets the price in gross or net, according to the type of customer.
     * @param amount the amount to be converted.
     * @param taxRate the tax rate that applies to this price.
     * @param customer the customer to which the price is applied.
     * @return the calculated net price for B2B customers, or gross price otherwise.
     */
    public static Money customerPrice(Money amount, TaxRate taxRate, Optional<ShopCustomer> customer) {
        if (customer.isPresent() && customer.get().isB2B()) {
            return netPrice(amount, taxRate);
        } else {
            return grossPrice(amount, taxRate);
        }
    }

    /**
     * Gets the gross price for the given price and applied tax rate.
     * @param amount the amount to be converted.
     * @param taxRate the tax rate that applies to this price.
     * @return the calculated gross price, or the same amount if taxes are included in the price.
     */
    public static Money grossPrice(Money amount, TaxRate taxRate) {
        if (taxRate.isIncludedInPrice()) {
            return amount;
        } else {
            return amount.plus(amount.multiply(taxRate.getAmount()));
        }
    }

    /**
     * Gets the net price for the given price and applied tax rate.
     * @param amount the amount to be converted.
     * @param taxRate the tax rate that applies to this price.
     * @return the calculated net price, or the same amount if taxes are not included in the price.
     */
    public static Money netPrice(Money amount, TaxRate taxRate) {
        if (!taxRate.isIncludedInPrice()) {
            return amount;
        } else {
            return netPrice(amount, taxRate.getAmount());
        }
    }

    /**
     * Gets the net price for the given gross price and tax rate amount applied.
     * @param grossAmount the gross amount to be converted.
     * @param taxRate the tax rate to be applied, e.g. 0.19 for a tax rate of 19%.
     * @return the calculated net price.
     */
    public static Money netPrice(Money grossAmount, double taxRate) {
        return grossAmount.multiply(1 / (1 + taxRate));
    }

}
