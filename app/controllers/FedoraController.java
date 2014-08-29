package controllers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import models.CommonDataBuilder;
import models.ShopCart;
import models.ShopCustomer;
import models.UserContext;
import play.Configuration;
import play.Logger;
import play.Play;
import play.i18n.Lang;
import play.libs.F;
import play.mvc.Http;

import com.neovisionaries.i18n.CountryCode;
import services.CartService;
import services.CheckoutService;
import services.CustomerService;
import services.OrderService;
import sphere.ShopController;

/**
 * An application specific controller.
 * Since we want to show a standard web shop it contains categories.
 */
public class FedoraController extends ShopController {
    private CountryCode country;

    private final CustomerService customerService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CheckoutService checkoutService;

    protected FedoraController(final CustomerService customerService, final CartService cartService,
                               final OrderService orderService, final CheckoutService checkoutService) {
        this.customerService = customerService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.checkoutService = checkoutService;
    }

    public CustomerService customerService() {
        return customerService;
    }

    public CartService cartService() {
        return cartService;
    }

    public OrderService orderService() {
        return orderService;
    }

    public CheckoutService checkoutService() {
        return checkoutService;
    }

    protected final CommonDataBuilder data(UserContext userContext) {
       return CommonDataBuilder.of(userContext, Lang.availables());
    }

    protected final UserContext userContext(ShopCart currentCart, Optional<ShopCustomer> registeredCustomer) {
        return UserContext.of(lang(), country(), currentCart, registeredCustomer);
    }

    protected final F.Promise<ShopCart> cart() {
        return cartService.fetchCurrent();
    }

    protected final F.Promise<Optional<ShopCustomer>> customer() {
        return customerService.fetchCurrent();
    }

    protected final CountryCode country() {
        return country(request(), Play.application().configuration());
    }

    protected final void changeCountry(CountryCode country) {
        changeCountry(country, response(), Play.application().configuration());
    }

    /**
     * Sets the country associated with the user.
     * @param country the desired country for the user.
     * @param response the HTTP response to the user.
     * @param config the configuration of this shop.
     */
    protected void changeCountry(CountryCode country, Http.Response response, Configuration config) {
        if (availableCountries(config).contains(country)) {
            this.country = country;
            response.setCookie(countryCookieName(config), country.getAlpha2());
        }
    }

    /**
     * Gets the country associated with the user.
     * @param request the incoming HTTP request.
     * @param config the configuration of this shop.
     * @return the current country if any, or the country stored in the user's cookie, or the default country of the shop if none stored.
     */
    protected CountryCode country(Http.Request request, Configuration config) {
        if (this.country != null) {
            this.country = countryInCookie(request, config).or(defaultCountry(config));
        }
        return this.country;
    }

    /**
     * Gets the list of available countries for this shop, as defined in the configuration file.
     * @param config the configuration of this shop.
     * @return the list of available countries.
     */
    protected List<CountryCode> availableCountries(Configuration config) {
        List<String> configCountries = config.getStringList("sphere.countries");
        List<CountryCode> countries = new ArrayList<>();
        for (String configCountry : configCountries) {
            Optional<CountryCode> country = parseCountryCode(configCountry);
            if (country.isPresent()) {
                countries.add(country.get());
            }
        }
        return countries;
    }

    /**
     * Gets the country stored in the user's cookie.
     * @param request the incoming HTTP request.
     * @param config the configuration of this shop.
     * @return the country stored in the user's cookie, or absent if none stored.
     */
    protected Optional<CountryCode> countryInCookie(Http.Request request, Configuration config) {
        final Http.Cookie cookieCountry = request.cookie(countryCookieName(config));
        if (cookieCountry != null) {
            return parseCountryCode(cookieCountry.value());
        } else {
            return Optional.absent();
        }
    }

    /**
     * Gets the default country for this shop, as defined in the configuration file.
     * @param config the configuration of this shop.
     * @return the first valid country defined in the configuration file.
     */
    protected CountryCode defaultCountry(Configuration config) {
        List<CountryCode> availableCountries = availableCountries(config);
        if (!availableCountries.isEmpty()) {
            return availableCountries.get(0);
        } else {
            throw new RuntimeException("No valid country defined in configuration file");
        }
    }

    /**
     * Gets the name of the cookie containing country information.
     * @param config the configuration of this shop.
     * @return the name of the cookie as defined in the configuration file, or the default name if none defined.
     */
    protected String countryCookieName(Configuration config) {
        return config.getString("shop.country.cookie", "SHOP_COUNTRY");
    }

    // TODO move to a utils class
    /**
     * Parses a country code as string.
     * @param countryCodeAsString the string representing a country code.
     * @return the country code represented in the string, or absent if it does not correspond to a valid country.
     */
    public static Optional<CountryCode> parseCountryCode(String countryCodeAsString) {
        try {
            return Optional.of(CountryCode.valueOf(countryCodeAsString));
        } catch (IllegalArgumentException e) {
            Logger.warn("Invalid country " + countryCodeAsString, e);
            return Optional.absent();
        }
    }
}
