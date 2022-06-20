package at.uibk.dps.rm.service.resource;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


@ProxyGen
public interface ResourceTypeService {

    static ResourceTypeService create(Vertx vertx) {
        return new ResourceTypeServiceImpl(vertx);
    }

    static ResourceTypeService createProxy(Vertx vertx, String address) {
        return new ResourceTypeServiceVertxEBProxy(vertx, address);
    }

    Future<Void> save(JsonObject resourceType);

    Future<JsonObject> findOne(int id);

    Future<JsonArray> findAll();
}
