package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class ResourceServiceImpl implements ResourceService{

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }
    @Override
    public Future<JsonObject> save(JsonObject data) {
        return null;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(resourceRepository.findById(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(resourceRepository.findAll())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Resource resource: result) {
                    objects.add(JsonObject.mapFrom(resource));
                }
                return new JsonArray(objects);
            });
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
