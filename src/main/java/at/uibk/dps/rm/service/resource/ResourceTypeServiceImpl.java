package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.service.database.DatabaseService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ResourceTypeServiceImpl implements ResourceTypeService {

    private final DatabaseService databaseService;

    public ResourceTypeServiceImpl(Vertx vertx) {
        databaseService = DatabaseService.createProxy(vertx, "database-service-address");
    }

    @Override
    public Future<Void> save(JsonObject resourceType) {
        return null;
    }

    @Override
    public Future<JsonObject> findOne(int id) {
        return null;
    }

    @Override
    public Future<JsonArray> findAll() {
        return databaseService.findAll("ResourceType");
    }
}
