package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface ResourceService {
    @GenIgnore
    static ResourceService create(ResourceRepository resourceRepository) {
        return new ResourceServiceImpl(resourceRepository);
    }

    @GenIgnore
    static ResourceService createProxy(Vertx vertx, String address) {
        return new ResourceServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> save(JsonObject data);

    Future<JsonObject> findOne(long id);

    Future<Boolean> existsOneByUrl(String url);

    Future<JsonArray> findAll();

    Future<Void> update(JsonObject data);

    Future<Void> delete(long id);
}
