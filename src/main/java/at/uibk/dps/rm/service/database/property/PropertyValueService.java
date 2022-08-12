package at.uibk.dps.rm.service.database.property;

import at.uibk.dps.rm.repository.PropertyValueRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface PropertyValueService extends ServiceInterface {

    @GenIgnore
    static PropertyValueService create(PropertyValueRepository propertyValueRepository) {
        return new PropertyValueServiceImpl(propertyValueRepository);
    }

    static PropertyValueService createProxy(Vertx vertx, String address) {
        return new PropertyValueServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByResource(long resourceId);

    Future<Boolean> existsOneByResourceAndProperty(long resourceId, long propertyId);

    Future<Void> deleteByResourceAndProperty(long resourceId, long propertyId);
}
