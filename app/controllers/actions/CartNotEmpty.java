package controllers.actions;

import controllers.routes;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import sphere.Sphere;

public class CartNotEmpty extends Action.Simple {

    /*
    * Checks whether the current cart is empty
    * */
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        if (Sphere.getInstance().currentCart().getQuantity() < 1) {
            ctx.flash().put("info", Messages.get(ctx.lang(), "cart.isEmpty"));
            return F.Promise.pure(redirect(routes.CartController.show()));
        }
        return delegate.call(ctx);
    }
}
