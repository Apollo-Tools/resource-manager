package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class ResourceServiceImpl implements ResourceService{

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }
    @Override
    public Future<JsonObject> save(JsonObject data) {
        Resource resource = new Resource();
        resource.setUrl(data.getString("url"));
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(data.getJsonObject("resource_type").getLong("type_id"));
        resource.setResourceType(resourceType);

        return Future
            .fromCompletionStage(resourceRepository.create(resource))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(resourceRepository.findByIdAndFetch(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneById(long id) {
        return Future
            .fromCompletionStage(resourceRepository.findById(id))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsOneByUrl(String url) {
        return Future
            .fromCompletionStage(resourceRepository.findByUrl(url))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsOneByResourceType(long typeId) {
        return Future
            .fromCompletionStage(resourceRepository.findByResourceType(typeId))
            .map(result -> !result.isEmpty());
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(resourceRepository.findAllAndFetch())
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
        Resource resource = data.mapTo(Resource.class);
        return Future
            .fromCompletionStage(resourceRepository.update(resource))
            .mapEmpty();
    }

    @Override
    public Future<Void> delete(long id) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        return Future
            .fromCompletionStage(resourceRepository.delete(resource))
            .mapEmpty();
    }
}
