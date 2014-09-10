
import controllers.BaseController;
import controllers.CheckoutController;
import controllers.CustomerController;
import play.GlobalSettings;
import services.*;
import sphere.Sphere;

import java.lang.reflect.Constructor;

public class Global extends GlobalSettings {

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
        Sphere sphere = Sphere.getInstance();
        CustomObjectService customObjectService = new CustomObjectServiceImpl(sphere);
        CheckoutService checkoutService = new CheckoutServiceImpl(sphere, customObjectService);
        return new OrderServiceImpl(sphere, checkoutService);
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
