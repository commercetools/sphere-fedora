import controllers.BaseController;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import services.*;
import sphere.Sphere;

import java.lang.reflect.Constructor;
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

    protected CategoryService createCategoryService() {
        return new CategoryServiceImpl(Sphere.getInstance());
    }

    protected ProductService createProductService() {
        return new ProductServiceImpl(Sphere.getInstance());
    }

    protected CustomerService createCustomerService() {
        Sphere sphere = Sphere.getInstance();
        CustomObjectService customObjectService = new CustomObjectServiceImpl(sphere);
        CheckoutService checkoutService = new CheckoutServiceImpl(sphere, customObjectService);
        CartService cartService = new CartServiceImpl(sphere, checkoutService);
        return new CustomerServiceImpl(sphere, cartService, customObjectService);
    }

    protected CartService createCartService() {
        Sphere sphere = Sphere.getInstance();
        CustomObjectService customObjectService = new CustomObjectServiceImpl(sphere);
        CheckoutService checkoutService = new CheckoutServiceImpl(sphere, customObjectService);
        return new CartServiceImpl(sphere, checkoutService);
    }

    protected OrderService createOrderService() {
        return new OrderServiceImpl(Sphere.getInstance());
    }

    protected CheckoutService createCheckoutService() {
        Sphere sphere = Sphere.getInstance();
        CustomObjectService customObjectService = new CustomObjectServiceImpl(sphere);
        return new CheckoutServiceImpl(sphere, customObjectService);
    }

    @Override
    public <A> A getControllerInstance(final Class<A> controllerClass) throws Exception {
        final A result;
        if (BaseController.class.isAssignableFrom(controllerClass)) {
            Constructor<A> constructor = controllerClass.getConstructor(CategoryService.class, ProductService.class,
                    CartService.class, CustomerService.class);
            result = constructor.newInstance(createCategoryService(), createProductService(), createCartService(),
                    createCustomerService());
        } else {
            result = super.getControllerInstance(controllerClass);
        }
        return result;
    }
}
