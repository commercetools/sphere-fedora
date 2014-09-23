package utils;

import io.sphere.client.model.Money;
import models.UserContext;
import org.apache.commons.lang3.StringUtils;
import play.i18n.Messages;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

public final class PrintUtils {

    public static String printMoney(UserContext context, Money money) {
        NumberFormat numberFormat = NumberFormat.getInstance(context.countryLocale());
        numberFormat.setMinimumFractionDigits(2);
        String printedAmount = numberFormat.format(money.getAmount());
        String printedCurrency = printCurrency(context, money.getCurrencyCode());
        return String.format("%s %s", printedAmount, printedCurrency);
    }

    public static String printCurrency(UserContext context, String currencyCode) {
        return Currency.getInstance(currencyCode).getSymbol(context.countryLocale());
    }

    public static String printSortOption(String sortOption) {
        String key = "pop.sort.option." + sortOption;
        if (Messages.isDefined(key)) {
            return Messages.get(key);
        } else {
            return sortOption;
        }
    }

    public static String printPriceOption(String priceOption) {
        String key = "pop.price.option." + priceOption;
        if (Messages.isDefined(key)) {
            return Messages.get(key);
        } else {
            return priceOption;
        }
    }

    public static String printAmountOption(String amountOption) {
        String key = "pop.amount.option." + amountOption;
        if (Messages.isDefined(key)) {
            return Messages.get(key);
        } else {
            return amountOption;
        }
    }

    public static String abbreviate(String text, int maxWidth) {
        if (text == null) return "";
        return StringUtils.abbreviate(text, maxWidth);
    }

    public static BigDecimal percentage(double amount) {
        return BigDecimal.valueOf(amount * 100).stripTrailingZeros();
    }
}
