package controllers.actions;

import play.core.j.JavaResultExtractor;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class SaveContext extends Action.Simple {

    /*
    * Saves the current URL in session to be used later as a return URL
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Call
        Result result = delegate.call(ctx);

        // After
        if (JavaResultExtractor.getStatus(result) == 200) {
            ctx.session().put("returnUrl", ctx.request().uri());
        }

        return result;
    }
}
