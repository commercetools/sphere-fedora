package controllers.urls;

import com.google.common.base.Joiner;
import play.mvc.Call;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static models.RequestParameters.QUERY_PARAM_LANG;

public class ShopCall extends Call {
    final Call call;
    Map<String, String> queryString;

    ShopCall(Call call) {
        this.call = call;
        this.queryString = new HashMap<String, String>();
    }

    public String url() {
        String url = call.url();
        String query = buildQueryString();
        if (!query.isEmpty() && !url.contains("?")) {
            url += "?";
        }
        return url + query;
    }

    private String buildQueryString() {
        return Joiner.on("&").withKeyValueSeparator("=").join(queryString);
    }

    public String method() {
        return call.method();
    }

    public ShopCall withLanguage(Locale locale) {
        withParameter(QUERY_PARAM_LANG, locale.getLanguage());
        return this;
    }

    public ShopCall withParameter(String key, String value) {
        queryString.put(key, value);
        return this;
    }

    public ShopCall withoutParameter(String key) {
        if (queryString.containsKey(key)) {
            queryString.remove(key);
        }
        return this;
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
