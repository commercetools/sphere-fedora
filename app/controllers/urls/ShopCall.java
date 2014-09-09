package controllers.urls;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import models.RequestParameters;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Call;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ShopCall extends Call {
    final Optional<Call> call;
    Map<String, String> queryString;

    ShopCall(Optional<Call> call) {
        this.call = call;
        this.queryString = new HashMap<String, String>();
    }

    public static ShopCall of(Call call) {
        return new ShopCall(Optional.of(call));
    }

    public static ShopCall empty() {
        return new ShopCall(Optional.<Call>absent());
    }

    public String url() {
        String url = "";
        if (call.isPresent()) {
            url = call.get().url();
        }
        String query = buildQueryString();
        return joinUrl(url, query);
    }

    public String method() {
        if (call.isPresent()) {
            return call.get().method();
        } else {
            return "GET";
        }
    }

    public ShopCall withLanguage(Locale locale) {
        withParameter(RequestParameters.QUERY_PARAM_LANG, locale.getLanguage());
        return this;
    }

    public ShopCall withCountry(CountryCode country) {
        withParameter(RequestParameters.QUERY_PARAM_COUNTRY, country.getAlpha2());
        return this;
    }

    public ShopCall withSort(String sortOption) {
        withParameter(RequestParameters.QUERY_PARAM_SORT, sortOption);
        return this;
    }

    public ShopCall withPrice(String priceOption) {
        withParameter(RequestParameters.QUERY_PARAM_PRICE, priceOption);
        return this;
    }

    public ShopCall withDisplay(String displayOption) {
        withParameter(RequestParameters.QUERY_PARAM_DISPLAY, displayOption);
        return this;
    }

    public ShopCall withAmount(String amountOption) {
        withParameter(RequestParameters.QUERY_PARAM_AMOUNT, amountOption);
        return this;
    }

    public ShopCall withoutSort() {
        withoutParameter(RequestParameters.QUERY_PARAM_SORT);
        return this;
    }

    public ShopCall withoutPrice() {
        withoutParameter(RequestParameters.QUERY_PARAM_PRICE);
        return this;
    }

    public ShopCall withoutDisplay() {
        withoutParameter(RequestParameters.QUERY_PARAM_DISPLAY);
        return this;
    }

    public ShopCall withoutAmount() {
        withoutParameter(RequestParameters.QUERY_PARAM_AMOUNT);
        return this;
    }

    public ShopCall withFilters(RequestParameters parameters) {
        for (String parameterKey : RequestParameters.filterParameters()) {
            Optional<String> parameterValue = parameters.getParameterValue(parameterKey);
            if (parameterValue.isPresent()) {
                this.withParameter(parameterKey, parameterValue.get());
            }
        }
        return this;
    }

    protected ShopCall withParameter(String key, String value) {
        try {
            String encodedValue = URLEncoder.encode(value, "UTF-8");
            queryString.put(key, encodedValue);
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected ShopCall withoutParameter(String key) {
        if (queryString.containsKey(key)) {
            queryString.remove(key);
        }
        return this;
    }

    protected String buildQueryString() {
        return Joiner.on("&").withKeyValueSeparator("=").join(queryString);
    }

    protected String joinUrl(String url, String query) {
        if (query.isEmpty()) {
            return url;
        } else {
            if (!url.contains("?")) {
                url += "?";
            } else if (!StringUtils.endsWith(url, "&")) {
                url += "&";
            }
            return url + query;
        }
    }

    @Override
    public String toString() {
        return url();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopCall shopCall = (ShopCall) o;

        if (call != null ? !call.equals(shopCall.call) : shopCall.call != null) return false;
        if (queryString != null ? !queryString.equals(shopCall.queryString) : shopCall.queryString != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = call != null ? call.hashCode() : 0;
        result = 31 * result + (queryString != null ? queryString.hashCode() : 0);
        return result;
    }
}
