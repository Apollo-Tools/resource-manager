package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is the implementation of the #ResourceService.
 *
 * @author matthi-g
 */
public class ResourceServiceImpl extends DatabaseServiceProxy<Resource> implements ResourceService {

    private final ResourceRepository resourceRepository;

    /**
     * Create an instance from the resourceRepository.
     *
     * @param resourceRepository the resource repository
     */
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

    @Override
    public Future<JsonArray> findAllBySLOs(List<String> metrics, List<Long> regionIds, List<Long> providerIds,
            List<Long> resourceTypeIds) {
        return Future
                .fromCompletionStage(resourceRepository.findAllBySLOs(metrics,
                    regionIds, providerIds, resourceTypeIds))
                .map(this::encodeResourceList);
    }

    @Override
    public Future<JsonArray> findAllByFunctionId(long functionId) {
        return Future
            .fromCompletionStage(resourceRepository.findAllByFunctionIdAndFetch(functionId))
            .map(this::encodeResourceList);
    }

    @Override
    public Future<JsonArray> findAllByEnsembleId(long ensembleId) {
        return Future
                .fromCompletionStage(resourceRepository.findAllByEnsembleId(ensembleId))
                .map(this::encodeResourceList);
    }

    @Override
    public Future<Boolean> existsAllByIdsAndResourceTypes(Set<Long> resourceIds, List<String> resourceTypes) {
        return Future
            .fromCompletionStage(resourceRepository.findAllByResourceIdsAndResourceTypes(resourceIds, resourceTypes))
            .map(result -> result.size() == resourceIds.size());
    }

    @Override
    public Future<JsonArray> findAllByResourceIds(List<Long> resourceIds) {
        return Future
            .fromCompletionStage(resourceRepository.findAllByResourceIdsAndFetch(resourceIds))
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
