package at.uibk.dps.rm.service.database;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen(concrete = false)
public interface ServiceInterface {
    Future<JsonObject> save(JsonObject data);

    Future<Void> saveAll(JsonArray data);

    Future<JsonObject> findOne(long id);

    Future<Boolean> existsOneById(long id);

    Future<JsonArray> findAll();

    Future<Void> update(JsonObject data);

    Future<Void> delete(long id);
}
