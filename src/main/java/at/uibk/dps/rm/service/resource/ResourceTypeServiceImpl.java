package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class ResourceTypeServiceImpl implements ResourceTypeService {
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceTypeServiceImpl(Vertx vertx, ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
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
        return Future.fromCompletionStage(
            resourceTypeRepository.findAll())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Object o: result) {
                    objects.add(JsonObject.mapFrom(o));
                }
                return new JsonArray(objects);
            });
    }
}
