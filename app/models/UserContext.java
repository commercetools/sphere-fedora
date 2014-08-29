package models;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import play.i18n.Lang;

import com.neovisionaries.i18n.CountryCode;

/**
 * A container for all information related to the current user, such as country, language, cart and associated customer.
 */
public class UserContext {
    private final Lang lang;
    private final Locale fallbackLocale = Locale.ENGLISH;
    private final List<Locale> locales;
    private final CountryCode countryCode;
    private ShopCart cart;
    private Optional<ShopCustomer> registeredCustomer;

    private UserContext(final Lang lang, final CountryCode countryCode, final ShopCart cart,
                        final Optional<ShopCustomer> registeredCustomer) {
        this.lang = lang;
        this.locales = Lists.newArrayList(lang.toLocale(), fallbackLocale);
        this.countryCode = countryCode;
        this.cart = cart;
        this.registeredCustomer = registeredCustomer;
    }

    public static UserContext of(final Lang lang, final CountryCode countryCode, final ShopCart cart,
                                 final Optional<ShopCustomer> registeredCustomer) {
        return new UserContext(lang, countryCode, cart, registeredCustomer);
    }

    public Lang lang() {
        return lang;
    }

    public Locale locale() {
        return lang.toLocale();
    }

    public Locale fallbackLocale() {
        return fallbackLocale;
    }

    public List<Locale> locales() {
        return locales;
    }

    public CountryCode country() {
        return countryCode;
    }

    public ShopCart cart() {
        return cart;
    }

    public Optional<ShopCustomer> customer() {
        return registeredCustomer;
    }

    public boolean isB2B() {
        return registeredCustomer.isPresent() && registeredCustomer.get().isB2B();
    }

    public boolean isLoggedIn() {
        return registeredCustomer.isPresent();
    }
}
