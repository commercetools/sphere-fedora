
import com.google.inject.Guice;
import com.google.inject.Injector;
import controllers.BaseController;
import controllers.CartController;
import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

import java.lang.reflect.Method;

public class Global extends GlobalSettings {

    private Injector injector;

    @Override
    public void onStart(final Application application) {
        super.onStart(application);
        injector = createInjector(application);
    }

    @Override
    public <A> A getControllerInstance(final Class<A> controllerClass) throws Exception {
        return injector.getInstance(controllerClass);
    }

    @Override
    public Action onRequest(final Http.Request request, final Method actionMethod) {
        return new Action.Simple() {
            @Override
            public F.Promise<SimpleResult> call(final Http.Context ctx) throws Throwable {
                final BaseController controller = injector.getInstance(CartController.class);
                controller.changeCountryOnRequest(request, ctx);
                controller.changeLanguageOnRequest(request);
                return delegate.call(ctx);
            }
        };
    }

    private Injector createInjector(final Application application) {
        return Guice.createInjector(new ProductionModule(application));
    }
}
