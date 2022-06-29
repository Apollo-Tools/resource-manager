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
    static ResourceTypeService create(ResourceTypeRepository resourceTypeRepository) {
        return new ResourceTypeServiceImpl(resourceTypeRepository);
    }

    @GenIgnore
    static ResourceTypeService createProxy(Vertx vertx, String address) {
        return new ResourceTypeServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> save(JsonObject data);

    Future<JsonObject> findOne(long id);

    Future<Boolean> existsOneById(long id);

    Future<Boolean> existsOneByResourceType(String resourceType);

    Future<JsonArray> findAll();

    Future<Void> update(JsonObject data);

    Future<Void> delete(long id);
}
