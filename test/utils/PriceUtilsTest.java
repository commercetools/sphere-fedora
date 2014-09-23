package utils;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.Price;
import io.sphere.client.shop.model.TaxRate;
import models.ShopCustomer;
import org.junit.Test;

import java.math.BigDecimal;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.PriceUtils.customerPrice;
import static utils.PriceUtils.grossPrice;
import static utils.PriceUtils.netPrice;

public class PriceUtilsTest {

    @Test
    public void shouldCalculateNetPrice() {
        Money grossAmount = netPrice(money(10), 0.19);
        assertThat(grossAmount).isEqualTo(money(8.4));
    }

    @Test
    public void shouldCalculateGrossPrice() {
        Money grossAmount = grossPrice(money(10), 0.19);
        assertThat(grossAmount).isEqualTo(money(11.9));
    }

    @Test
    public void shouldCalculateNetPriceWithIncludedTax() {
        Money netAmount = netPrice(money(10), taxRateWithTaxIncluded(0.19));
        assertThat(netAmount).isEqualTo(money(8.4));
    }

    @Test
    public void shouldCalculateNetPriceWithExcludedTax() {
        Money netAmount = netPrice(money(10), taxRateWithTaxExcluded(0.19));
        assertThat(netAmount).isEqualTo(money(10));
    }

    @Test
    public void shouldCalculateGrossPriceWithIncludedTax() {
        Money grossAmount = grossPrice(money(10), taxRateWithTaxIncluded(0.19));
        assertThat(grossAmount).isEqualTo(money(10));
    }

    @Test
    public void shouldCalculateGrossPriceWithExcludedTax() {
        Money grossAmount = grossPrice(money(10), taxRateWithTaxExcluded(0.19));
        assertThat(grossAmount).isEqualTo(money(11.9));
    }

    @Test
    public void shouldCalculateGrossPriceForAnonymousCustomers() {
        Money customerPrice = customerPrice(money(10), taxRateWithTaxIncluded(0.19), anonymousCustomer());
        assertThat(customerPrice).isEqualTo(money(10));
    }

    @Test
    public void shouldCalculateGrossPriceForB2CCustomers() {
        Money customerPrice = customerPrice(money(10), taxRateWithTaxIncluded(0.19), b2cCustomer());
        assertThat(customerPrice).isEqualTo(money(10));
    }

    @Test
    public void shouldCalculateNetPriceForB2BCustomers() {
        Money customerPrice = customerPrice(money(10), taxRateWithTaxIncluded(0.19), b2bCustomer());
        assertThat(customerPrice).isEqualTo(money(8.4));
    }

    private Money money(double moneyAmount) {
        return new Money(BigDecimal.valueOf(moneyAmount), "EUR");
    }

    private TaxRate taxRateWithTaxIncluded(double taxRateAmount) {
        return taxRate(taxRateAmount, true);
    }

    private TaxRate taxRateWithTaxExcluded(double taxRateAmount) {
        return taxRate(taxRateAmount, false);
    }

    private TaxRate taxRate(double taxRateAmount, boolean isTaxIncludedInPrice) {
        return TaxRate.create("taxRate", taxRateAmount, isTaxIncludedInPrice, CountryCode.DE, "");
    }

    private Optional<ShopCustomer> anonymousCustomer() {
        return Optional.absent();
    }

    private Optional<ShopCustomer> b2cCustomer() {
        ShopCustomer customer = mock(ShopCustomer.class);
        when(customer.isB2B()).thenReturn(false);
        return Optional.of(customer);
    }

    private Optional<ShopCustomer> b2bCustomer() {
        ShopCustomer customer = mock(ShopCustomer.class);
        when(customer.isB2B()).thenReturn(true);
        return Optional.of(customer);
    }
}
