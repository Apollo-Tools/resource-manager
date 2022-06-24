package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ResourceServiceImpl implements ResourceService{

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(Vertx vertx, ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }
    @Override
    public Future<JsonObject> save(JsonObject data) {
        return null;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return null;
    }

    @Override
    public Future<JsonArray> findAll() {
        return null;
    }

    @Override
    public Future<Void> update(JsonObject data) {
        return null;
    }

    @Override
    public Future<Void> delete(long id) {
        return null;
    }
}
