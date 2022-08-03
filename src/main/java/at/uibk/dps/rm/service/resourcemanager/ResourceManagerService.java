package at.uibk.dps.rm.service.resourcemanager;

import at.uibk.dps.rm.resourcemanager.ResourceStore;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface ResourceManagerService {
    @GenIgnore
    static ResourceManagerService create(ResourceStore resourceStore) {
        return new ResourceManagerServiceImpl(resourceStore);
    }

    static ResourceManagerService createProxy(Vertx vertx, String address) {
        return new ResourceManagerServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> getOne(long id);

    Future<JsonArray> getAll();
}
