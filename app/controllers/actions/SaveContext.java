package controllers.actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

public class SaveContext extends Action.Simple {

    /*
    * Saves the current URL in session to be used later as a return URL
    * */
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        ctx.session().put("returnUrl", ctx.request().uri());
        return delegate.call(ctx);
    }
}
