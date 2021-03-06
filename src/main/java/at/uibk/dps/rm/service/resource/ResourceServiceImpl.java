package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class ResourceServiceImpl extends ServiceProxy<Resource> implements ResourceService{

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        super(resourceRepository, Resource.class);
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(resourceRepository.findByIdAndFetch(id))
            .map(JsonObject::mapFrom);
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
}
