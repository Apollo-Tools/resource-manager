package at.uibk.dps.rm.service.database.property;

import at.uibk.dps.rm.repository.property.PropertyRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface PropertyService extends ServiceInterface {
    @GenIgnore
    static PropertyService create(PropertyRepository propertyRepository) {
        return new PropertyServiceImpl(propertyRepository);
    }

    static PropertyService createProxy(Vertx vertx, String address) {
        return new PropertyServiceVertxEBProxy(vertx, address);
    }

    Future<Boolean> existsOneByProperty(String property);
}
