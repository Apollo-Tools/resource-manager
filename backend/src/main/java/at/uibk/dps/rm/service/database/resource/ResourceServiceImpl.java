package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
        return Future.fromCompletionStage(findOne)
            .map(resource -> {
                if (resource != null) {
                    resource.getRegion().getResourceProvider().setProviderPlatforms(null);
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
        CompletionStage<List<Resource>> findAll = withSession(session -> {
            List<CompletableFuture<Void>> checkSLOs = sloRequest.getServiceLevelObjectives().stream().map(slo ->
                metricRepository.findByMetric(session, slo.getName())
                    .thenAccept(metric -> validateSLOType(slo, metric))
                    .toCompletableFuture()
                )
                .collect(Collectors.toList());
            return CompletableFuture.allOf(checkSLOs.toArray(CompletableFuture[]::new))
                .thenCompose(result -> {
                    List<String> sloNames = sloRequest.getServiceLevelObjectives().stream()
                        .map(ServiceLevelObjective::getName)
                        .collect(Collectors.toList());
                    return repository.findAllBySLOs(session, sloNames, sloRequest.getEnvironments(),
                        sloRequest.getResourceTypes(), sloRequest.getPlatforms(), sloRequest.getRegions(),
                        sloRequest.getProviders())
                        .toCompletableFuture();
                })
                .thenApply(resources -> SLOCompareUtility.filterAndSortResourcesBySLOs(resources,
                    sloRequest.getServiceLevelObjectives()));
        });
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
            resource.getRegion().getResourceProvider().setProviderPlatforms(null);
            objects.add(JsonObject.mapFrom(resource));
        }
        return new JsonArray(objects);
    }

    private void validateSLOType(ServiceLevelObjective slo, Metric metric) {
        ServiceResultValidator.checkFound(metric, ServiceLevelObjective.class);
        String sloValueType = slo.getValue().get(0).getSloValueType().name();
        String metricValueType = metric.getMetricType().getType().toUpperCase();
        boolean checkForTypeMatch = sloValueType.equals(metricValueType);
        if (!checkForTypeMatch) {
            throw new BadInputException("bad input type for service level objective " + slo.getName());
        }
    }
}
