import play.GlobalSettings;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

import java.lang.reflect.Method;

public class Global extends GlobalSettings {

    @Override
    public Action onRequest(Http.Request arg0, Method arg1) {
        return new Action.Simple() {
            final static String LANG_QUERY = "lang";

            public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
                if (ctx.request().queryString().containsKey(LANG_QUERY)) {
                    String lang = ctx.request().getQueryString(LANG_QUERY);
                    if (!ctx.changeLang(lang))
                        return toPrevPage(ctx);
                }
                return delegate.call(ctx);
            }

            private F.Promise<SimpleResult> toPrevPage(Http.Context ctx) {
                String url = ctx.request().getHeader("referer");
                if (url == null) url = "/";
                return F.Promise.pure(redirect(url));
            }
        };
    }

}
