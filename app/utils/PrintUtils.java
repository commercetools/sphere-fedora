package utils;

import io.sphere.client.model.Money;

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

}
