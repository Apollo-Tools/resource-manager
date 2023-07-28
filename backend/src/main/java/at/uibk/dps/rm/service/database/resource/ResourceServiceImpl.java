package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.SubResource;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ResourceService.
 *
 * @author matthi-g
 */
public class ResourceServiceImpl extends DatabaseServiceProxy<Resource> implements ResourceService {

    private final ResourceRepository repository;

    private final MetricRepository metricRepository;

    /**
     * Create an instance from the resourceRepository.
     *
     * @param repository the resource repository
     */
    public ResourceServiceImpl(ResourceRepository repository, MetricRepository metricRepository,
            SessionFactory sessionFactory) {
        super(repository, Resource.class, sessionFactory);
        this.repository = repository;
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Resource> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return sessionToFuture(findOne)
            .map(resource -> {
                if (resource instanceof SubResource) {
                    SubResourceDTO subResource = new SubResourceDTO((SubResource) resource);
                    return JsonObject.mapFrom(subResource);
                }
                return JsonObject.mapFrom(resource);
            });
    }

    @Override
    public Future<Boolean> existsOneByResourceType(long typeId) {
        CompletionStage<List<Resource>> findAll = withSession(session ->
            repository.findByResourceType(session, typeId));
        return Future
            .fromCompletionStage(findAll)
            .map(result -> !result.isEmpty());
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Resource>> findAll = withSession(repository::findAllAndFetch);
        return Future
            .fromCompletionStage(findAll)
            .map(this::encodeResourceList);
    }

    @Override
    public Future<JsonArray> findAllBySLOs(JsonObject data) {
        SLORequest sloRequest = data.mapTo(SLORequest.class);
        CompletionStage<List<Resource>> findAll = withSession(session ->
            new SLOUtility(repository, metricRepository).findAndFilterResourcesBySLOs(session, sloRequest));
        return sessionToFuture(findAll).map(this::encodeResourceList);
    }

    @Override
    public Future<JsonArray> findAllByEnsembleId(long ensembleId) {
        CompletionStage<List<Resource>> findAll = withSession(session ->
            repository.findAllByEnsembleId(session, ensembleId));
        return Future.fromCompletionStage(findAll)
            .map(this::encodeResourceList);
    }

    @Override
    public Future<Boolean> existsAllByIdsAndResourceTypes(Set<Long> resourceIds, List<String> resourceTypes) {
        CompletionStage<List<Resource>> findAll = withSession(session ->
            repository.findAllByResourceIdsAndResourceTypes(session, resourceIds, resourceTypes));
        return Future
            .fromCompletionStage(findAll)
            .map(result -> result.size() == resourceIds.size());
    }

    @Override
    public Future<JsonArray> findAllByResourceIds(List<Long> resourceIds) {
        CompletionStage<List<Resource>> findAll = withSession(session ->
            repository.findAllByResourceIdsAndFetch(session, resourceIds));
        return Future.fromCompletionStage(findAll)
            .map(this::encodeResourceList);
    }

    private JsonArray encodeResourceList(List<Resource> resourceList) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (Resource resource: resourceList) {
            if (resource instanceof SubResource) {
                SubResourceDTO subResource = new SubResourceDTO((SubResource) resource);
                objects.add(JsonObject.mapFrom(subResource));
            } else {
                objects.add(JsonObject.mapFrom(resource));
            }
        }
        return new JsonArray(objects);
    }
}
