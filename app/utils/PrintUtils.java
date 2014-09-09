package utils;

import io.sphere.client.model.Money;
import org.apache.commons.lang3.StringUtils;
import play.i18n.Messages;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class PrintUtils {

    public static String printMoney(Locale locale, Money money) {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setMinimumFractionDigits(2);
        String printedAmount = numberFormat.format(money.getAmount());
        return String.format("%s %s", printedAmount, printCurrency(locale, money.getCurrencyCode()));
    }

    public static String printCurrency(Locale locale, String currencyCode) {
        return Currency.getInstance(currencyCode).getSymbol(locale);
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

    public static BigDecimal getPercentage(double amount) {
        return BigDecimal.valueOf(amount * 100).stripTrailingZeros();
    }
}
