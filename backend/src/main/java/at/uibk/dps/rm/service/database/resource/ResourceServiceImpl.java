package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.Hibernate;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ResourceService.
 *
 * @author matthi-g
 */
public class ResourceServiceImpl extends DatabaseServiceProxy<Resource> implements ResourceService {

    private final ResourceRepository repository;

    private final RegionRepository regionRepository;

    private final MetricRepository metricRepository;

    /**
     * Create an instance from the resourceRepository.
     *
     * @param repository the resource repository
     */
    public ResourceServiceImpl(ResourceRepository repository, RegionRepository regionRepository,
            MetricRepository metricRepository, SessionFactory sessionFactory) {
        super(repository, Resource.class, sessionFactory);
        this.repository = repository;
        this.regionRepository = regionRepository;
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        String name = data.getString("name");
        long regionId = data.getJsonObject("region").getLong("region_id");
        long platformId = data.getJsonObject("platform").getLong("platform_id");
        MainResource resource = new MainResource();
        CompletionStage<Resource> save = withTransaction(session ->
            repository.findByNameAndRegionId(session, name, regionId)
                .thenCompose(exitingResource -> {
                    ServiceResultValidator.checkExists(exitingResource, Resource.class);
                    return regionRepository.findByRegionIdAndPlatformId(session, regionId, platformId);
                })
                .thenCompose(region -> {
                    ServiceResultValidator.checkFound(region, "Platform not found for the selected region");
                    resource.setName(name);
                    resource.setRegion(region);
                    return session.find(Platform.class, platformId);
                })
                .thenCompose(platform -> {
                    ServiceResultValidator.checkFound(platform, Platform.class);
                    resource.setPlatform(platform);
                    return session.persist(resource);
                })
                .thenApply(res -> resource));
        return sessionToFuture(save).map(JsonObject::mapFrom);
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
    public Future<JsonArray> findAllSubResources(long resourceId) {
        CompletionStage<List<SubResource>> findAll = withSession(session ->
            repository.findAllSubresources(session, resourceId));
        return sessionToFuture(findAll)
            .map(resources -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (SubResource resource: resources) {
                    objects.add(JsonObject.mapFrom(resource));
                }
                return new JsonArray(objects);
            });
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
                MainResource mainResource = (MainResource) resource;
                if (Hibernate.isInitialized(mainResource.getSubResources())) {
                    mainResource.getSubResources().forEach(subResource -> subResource.setMainResource(null));
                }
                objects.add(JsonObject.mapFrom(resource));
            }
        }
        return new JsonArray(objects);
    }
}
