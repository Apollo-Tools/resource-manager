package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ResourceServiceImpl extends ServiceProxy<Resource> implements ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        super(resourceRepository, Resource.class);
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(resourceRepository.findByIdAndFetch(id))
            // TODO: fix
            .map(result -> {
                if (result != null) {
                    result.setMetricValues(null);
                }
                return JsonObject.mapFrom(result);
            });
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
            .map(this::encodeResourceList);
    }

    @Deprecated
    @Override
    public Future<JsonArray> findAllUnreserved() {
        return Future
            .fromCompletionStage(resourceRepository.findAllAndFetch())
            .map(this::encodeResourceList);
    }


    @Override
    public Future<JsonArray> findAllByFunctionAndMultipleMetrics(long functionId, List<String> metrics) {
        return Future
                .fromCompletionStage(resourceRepository.findByFunctionAndMultipleMetricsAndFetch(functionId, metrics))
                .map(this::encodeResourceList);
    }

    @Override
    public Future<JsonArray> findAllByFunctionId(long functionId) {
        return Future
            .fromCompletionStage(resourceRepository.findAllByFunctionIdAndFetch(functionId))
            .map(this::encodeResourceList);
    }

    private JsonArray encodeResourceList(List<Resource> resourceList) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (Resource resource: resourceList) {
            objects.add(JsonObject.mapFrom(resource));
        }
        return new JsonArray(objects);
    }
}
