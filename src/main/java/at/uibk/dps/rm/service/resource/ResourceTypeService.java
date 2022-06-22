package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


@ProxyGen
@VertxGen
public interface ResourceTypeService {
    @GenIgnore
    static ResourceTypeService create(Vertx vertx, ResourceTypeRepository resourceTypeRepository) {
        return new ResourceTypeServiceImpl(vertx, resourceTypeRepository);
    }

    @GenIgnore
    static ResourceTypeService createProxy(Vertx vertx, String address) {
        return new ResourceTypeServiceVertxEBProxy(vertx, address);
    }

    Future<Void> save(JsonObject resourceType);

    Future<JsonObject> findOne(int id);

    Future<JsonArray> findAll();
}
