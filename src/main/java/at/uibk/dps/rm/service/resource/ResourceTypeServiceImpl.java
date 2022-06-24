package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
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
    public Future<JsonObject> save(JsonObject data) {
        ResourceType resourceType = new ResourceType();
        resourceType.setResource_type(data.getString("resource_type"));

        return Future
            .fromCompletionStage(resourceTypeRepository.create(resourceType))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(resourceTypeRepository.findById(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> findOneByResourceType(String resourceType) {
        return Future
            .fromCompletionStage(resourceTypeRepository.findByResourceType(resourceType))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(resourceTypeRepository.findAll())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ResourceType rt: result) {
                    objects.add(JsonObject.mapFrom(rt));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Void> update(JsonObject data) {
        ResourceType resourceType = data.mapTo(ResourceType.class);
        return Future
            .fromCompletionStage(resourceTypeRepository.update(resourceType))
            .mapEmpty();
    }

    @Override
    public Future<Void> delete(long id) {
        return Future
            .fromCompletionStage(resourceTypeRepository.delete(id))
            .mapEmpty();
    }
}
