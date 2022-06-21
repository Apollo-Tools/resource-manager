package at.uibk.dps.rm.service.database;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface DatabaseService {
    @GenIgnore
    static DatabaseService create(JsonObject config) {
        return new DatabaseServiceImpl(config);
    }

    @GenIgnore
    static DatabaseService createProxy(Vertx vertx, String address) {
        return new DatabaseServiceVertxEBProxy(vertx, address);
    }

    Future<Void> persist(JsonObject data);

    Future<JsonObject> findById(int id);

    Future<JsonArray> findAll(String table);

    Future<Void> update(JsonObject data);

    Future<Void> remove(JsonObject data);
}
