package controllers;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import models.*;
import play.Configuration;
import play.Logger;
import play.Play;
import play.api.mvc.Call;
import play.i18n.Lang;
import play.libs.F;
import play.mvc.Content;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import services.CartService;
import services.CategoryService;
import services.CustomerService;
import services.ProductService;
import sphere.ShopController;

import java.util.*;

import utils.AsyncUtils;

import static models.RequestParameters.QUERY_PARAM_COUNTRY;
import static models.RequestParameters.QUERY_PARAM_LANG;

/**
 * The common functionality for all the shop controllers.
 */
public class BaseController extends ShopController {
    private static final String COUNTRY_SESSION = "country_session";
    private final CategoryService categoryService;
    private final ProductService productService;
    private final CartService cartService;
    private final CustomerService customerService;

    protected BaseController(final CategoryService categoryService, final ProductService productService,
                             final CartService cartService, final CustomerService customerService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.cartService = cartService;
        this.customerService = customerService;
    }

    public final void changeCountryOnRequest(final Http.Request request, final Http.Context context) {
        String country = request.getQueryString(QUERY_PARAM_COUNTRY);
        if (country != null) {
            changeCountry(country, context);
        }
    }

    public final void changeLanguageOnRequest(final Http.Request request) {
        String lang = request.getQueryString(QUERY_PARAM_LANG);
        Optional<String> languageCode = getValidLanguageCode(lang);
        if (languageCode.isPresent()) {
            changeLang(languageCode.get());
        }
    }

    protected Functions f() {
        return new Functions();
    }

    protected CategoryService categoryService() {
        return categoryService;
    }

    protected ProductService productService() {
        return productService;
    }

    protected CartService cartService() {
        return cartService;
    }

    protected CustomerService customerService() {
        return customerService;
    }

    protected final CommonDataBuilder data() {
        final UserContext context = userContext(cart(), customer());
        return data(context);
    }

    protected final CommonDataBuilder data(UserContext userContext) {
        List<CountryCode> availableCountry = availableCountries(Play.application().configuration());
        return CommonDataBuilder.of(userContext, Lang.availables(), availableCountry, categoryService.getRoots())
                .withSelectableAttributes(selectableAttributeNames());
    }

    protected final UserContext userContext(ShopCart currentCart, Optional<ShopCustomer> registeredCustomer) {
        return UserContext.of(lang(), country(), currentCart, registeredCustomer);
    }

    protected final ShopCart cart() {
        return cartService.fetchCurrent().get(AsyncUtils.defaultTimeout());
    }

    protected final Optional<ShopCustomer> customer() {
        return customerService.fetchCurrent().get(AsyncUtils.defaultTimeout());
    }

    protected final Map<String, String[]> queryString() {
        return request().queryString();
    }

    protected final Locale locale() {
        return new Locale(lang().language());
    }

    protected final CountryCode country() {
        return country(request(), session(), Play.application().configuration());
    }

    private Optional<String> getValidLanguageCode(String lang) {
        if (lang != null) {
            for (Lang availableLang : Lang.availables()) {
                if (availableLang.language().equals(lang)) {
                    return Optional.of(availableLang.code());
                }
            }
        }
        return Optional.absent();
    }

    protected final void changeCountry(String country, Http.Context context) {
        Optional<CountryCode> countryCode = parseCountryCode(country);
        if (countryCode.isPresent()) {
            changeCountry(countryCode.get(), context, Play.application().configuration());
        }
    }

    /**
     * Sets the country associated with the user.
     * @param country the desired country for the user.
     * @param context the HTTP context of the user.
     * @param config the configuration of this shop.
     */
    protected void changeCountry(CountryCode country, Http.Context context, Configuration config) {
        if (availableCountries(config).contains(country)) {
            context.session().put(COUNTRY_SESSION, country.getAlpha2());
            context.response().setCookie(countryCookieName(config), country.getAlpha2());
            cartService.setCountry(cart(), country);
        }
    }

    /**
     * Gets the country associated with the user.
     * @param request the HTTP request from the user.
     * @param session the user session.
     * @param config the configuration of this shop.
     * @return the current country if any, or the country stored in the user's cookie, or the default country of the shop if none stored.
     */
    protected CountryCode country(Http.Request request, Http.Session session, Configuration config) {
        if (!session.containsKey(COUNTRY_SESSION)) {
            final CountryCode country = countryInCookie(request, config).or(defaultCountry(config));
            session.put(COUNTRY_SESSION, country.getAlpha2());
        }
        return parseCountryCode(session.get(COUNTRY_SESSION)).or(defaultCountry(config));
    }

    /**
     * Gets the list of available countries for this shop, as defined in the configuration file.
     * @param config the configuration of this shop.
     * @return the list of available countries.
     */
    protected List<CountryCode> availableCountries(Configuration config) {
        List<String> configCountries = config.getStringList("sphere.countries");
        List<CountryCode> countries = new ArrayList<CountryCode>();
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

    static Content showNotFoundPage(CommonDataBuilder data) {
        return views.html.notFoundView.render(data.build());
    }

    static List<String> selectableAttributeNames() {
        return Play.application().configuration().getStringList("attributes.selectable", Collections.<String>emptyList());
    }

    static String paymillPublicKey() {
        return Play.application().configuration().getString("paymill.key.public");
    }

    static String paymillPrivateKey() {
        return Play.application().configuration().getString("paymill.key.private");
    }

    protected Result redirectToReturnUrl() {
        return redirect(session("returnUrl"));
    }

    protected static final class Functions {
        private Functions() {
        }

        public static <T> F.Function<T, Result> redirect(final Call call) {
            return new F.Function<T, Result>() {
                @Override
                public Result apply(final T o) throws Throwable {
                    return Results.redirect(call);
                }
            };
        }

        public static <T> F.Function<T, Result> redirectWithFlash(final Call call, final String flashKey, final String flashValue) {
            return new F.Function<T, Result>() {
                @Override
                public Result apply(final T o) throws Throwable {
                    flash(flashKey, flashValue);
                    return Results.redirect(call);
                }
            };
        }
    }
}
