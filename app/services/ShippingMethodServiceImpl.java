package services;


import com.google.common.base.Optional;
import io.sphere.client.model.QueryResult;
import io.sphere.client.shop.model.ShippingMethod;
import play.libs.F;
import sphere.Sphere;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class ShippingMethodServiceImpl implements ShippingMethodService {
    private final Sphere sphere;

    @Inject
    public ShippingMethodServiceImpl(Sphere sphere) {
        this.sphere = sphere;
    }

    @Override
    public F.Promise<List<ShippingMethod>> getShippingMethods() {
        return sphere.shippingMethods().query().pageSize(500).fetchAsync()
                .map(new F.Function<QueryResult<ShippingMethod>, List<ShippingMethod>>() {
                    @Override
                    public List<ShippingMethod> apply(QueryResult<ShippingMethod> shippingMethodQueryResult) throws Throwable {
                        return shippingMethodQueryResult.getResults();
                    }
                });
    }

    @Override
    public F.Promise<Optional<ShippingMethod>> fetchById(String shippingMethodId) {
        return sphere.shippingMethods().byId(shippingMethodId).fetchAsync();
    }
}
