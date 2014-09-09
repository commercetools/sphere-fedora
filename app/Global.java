import com.google.common.base.Optional;
import controllers.BaseController;
import controllers.CheckoutController;
import controllers.CustomerController;
import play.GlobalSettings;
import play.i18n.Lang;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import services.*;
import sphere.Sphere;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static models.RequestParameters.QUERY_PARAM_LANG;

public class Global extends GlobalSettings {

    @Override
    public Action onRequest(Http.Request arg0, Method arg1) {
        return new Action.Simple() {

            public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
                String lang = ctx.request().getQueryString(QUERY_PARAM_LANG);
                Optional<String> languageCode = getValidLanguageCode(lang);
                if (languageCode.isPresent()) {
                    ctx.changeLang(languageCode.get());
                    return redirectToReferrer(ctx);
                } else {
                    return delegate.call(ctx);
                }
            }

            private F.Promise<SimpleResult> redirectToReferrer(Http.Context ctx) {
                String url = ctx.request().getHeader(Http.HeaderNames.REFERER);
                if (url == null) {
                    url = "/";
                }
                return F.Promise.pure(redirect(url));
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

    protected ShippingMethodService createShippingMethodService() {
        return new ShippingMethodServiceImpl(Sphere.getInstance());
    }

    protected CheckoutService createCheckoutService() {
        Sphere sphere = Sphere.getInstance();
        CustomObjectService customObjectService = new CustomObjectServiceImpl(sphere);
        return new CheckoutServiceImpl(sphere, customObjectService);
    }

    @Override
    public <A> A getControllerInstance(final Class<A> controllerClass) throws Exception {
        final A result;
        if (controllerClass.equals(CheckoutController.class)) {
            result = (A) new CheckoutController(createCategoryService(), createProductService(), createCartService(),
                    createCustomerService(), createCheckoutService(), createShippingMethodService());
        } else if (controllerClass.equals(CustomerController.class)) {
            result = (A) new CustomerController(createCategoryService(), createProductService(), createCartService(),
                    createCustomerService(), createOrderService());
        } else if (BaseController.class.isAssignableFrom(controllerClass)) {
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
