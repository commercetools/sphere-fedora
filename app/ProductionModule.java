import com.google.inject.AbstractModule;
import io.sphere.client.shop.SphereClient;
import play.Application;
import services.*;
import sphere.Sphere;


public class ProductionModule extends AbstractModule {

    public ProductionModule(final Application application) {
    }

    @Override
    protected void configure() {
        bind(CartService.class).to(CartServiceImpl.class);
        bind(CategoryService.class).to(CategoryServiceImpl.class);
        bind(CheckoutService.class).to(CheckoutServiceImpl.class);
        bind(CustomerService.class).to(CustomerServiceImpl.class);
        bind(CustomObjectService.class).to(CustomObjectServiceImpl.class);
        bind(OrderService.class).to(OrderServiceImpl.class);
        bind(ProductService.class).to(ProductServiceImpl.class);
        bind(ShippingMethodService.class).to(ShippingMethodServiceImpl.class);
        bind(Sphere.class).toInstance(Sphere.getInstance());
        bind(SphereClient.class).toInstance(Sphere.getInstance().client());
    }
}
