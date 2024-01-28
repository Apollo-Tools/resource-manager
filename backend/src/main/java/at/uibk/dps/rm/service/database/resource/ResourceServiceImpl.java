package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.K8sResourceUpdateUtility;
import at.uibk.dps.rm.service.database.util.LockedResourcesUtility;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.Hibernate;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.*;

/**
 * This is the implementation of the {@link ResourceService}.
 *
 * @author matthi-g
 */
public class ResourceServiceImpl extends DatabaseServiceProxy<Resource> implements ResourceService {

    private final ResourceRepository repository;
    private final RegionRepository regionRepository;

    /**
     * Create an instance from the resourceRepository.
     *
     * @param repository the resource repository
     */
    public ResourceServiceImpl(ResourceRepository repository, RegionRepository regionRepository,
            SessionManagerProvider smProvider) {
        super(repository, Resource.class, smProvider);
        this.repository = repository;
        this.regionRepository = regionRepository;
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Resource> findOne = smProvider.withTransactionMaybe( sm -> repository
            .findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
            .map(this::getResourceLockState));
        RxVertxHandler.handleSession(
            findOne.map(resource -> {
                if (resource instanceof SubResource) {
                    SubResourceDTO subResource = new SubResourceDTO((SubResource) resource);
                    return JsonObject.mapFrom(subResource);
                }
                //noinspection ReactiveStreamsNullableInLambdaInTransform
                return JsonObject.mapFrom(resource);
            }),
            resultHandler
        );
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sessionManager ->
            repository.findAllAndFetch(sessionManager)
                .flatMapObservable(Observable::fromIterable)
                .map(this::getResourceLockState).toList()
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResourceListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByNonMonitoredSLOs(JsonObject data, Handler<AsyncResult<JsonArray>> resultHandler) {
        SLORequest sloRequest = data.mapTo(SLORequest.class);
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sm ->
            new SLOUtility(repository).findResourcesByNonMonitoredSLOs(sm, sloRequest)
                .flatMapObservable(Observable::fromIterable)
                .map(this::getResourceLockState).toList()
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResourceListToJsonArray), resultHandler);
    }

    // TODO: remove main_resource
    @Override
    public void findAllSubResources(long resourceId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<SubResource>> findAll = smProvider.withTransactionSingle(sm ->
            repository.findAllSubresources(sm, resourceId)
                .flatMapObservable(Observable::fromIterable)
                .map(resource -> (SubResource) this.getResourceLockState(resource)).toList()
        );
        RxVertxHandler.handleSession(
            findAll.map(resources -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (SubResource resource: resources) {
                    objects.add(JsonObject.mapFrom(resource));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    @Override
    public void findAllLockedByDeployment(long deploymentId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sessionManager ->
            repository.findAllLockedByDeploymentId(sessionManager, deploymentId)
                .flatMapObservable(Observable::fromIterable)
                .map(this::getResourceLockState).toList()
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResourceListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByResourceIds(List<Long> resourceIds, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByResourceIdsAndFetch(sm, resourceIds)
            .flatMapObservable(Observable::fromIterable)
            .map(this::getResourceLockState).toList()
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResourceListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByPlatform(String platform, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllMainResourcesByPlatform(sm, platform)
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResourceListToJsonArray), resultHandler);
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        String name = data.getString("name");
        long regionId = data.getJsonObject("region").getLong("region_id");
        long platformId = data.getJsonObject("platform").getLong("platform_id");
        boolean isLockable = data.getBoolean("is_lockable");
        MainResource resource = new MainResource();
        Single<Resource> save = smProvider.withTransactionSingle(sm -> repository.findByName(sm, name)
            .flatMap(existingResource -> Maybe.<Region>error(new AlreadyExistsException(Resource.class)))
            .switchIfEmpty(regionRepository.findByRegionIdAndPlatformId(sm, regionId, platformId))
            .switchIfEmpty(Maybe.error(new NotFoundException("platform is not supported by the selected region")))
            .flatMap(region -> {
                resource.setName(name);
                resource.setRegion(region);
                return sm.find(Platform.class, platformId);
            })
            .switchIfEmpty(Single.error(new NotFoundException(Platform.class)))
            .flatMap(platform -> {
                resource.setPlatform(platform);
                resource.setIsLockable(isLockable);
                resource.setIsLocked(false);
                return sm.persist(resource);
            })
        );
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void delete(long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = smProvider.withTransactionCompletable(sm -> repository.findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }

    @Override
    public void update(long id, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = smProvider.withTransactionCompletable(sm -> sm.find(Resource.class, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
            .flatMapCompletable(resource -> {
                resource.setIsLockable(fields.getBoolean("is_lockable"));
                if (!resource.getIsLockable()) {
                    resource.setLockedByDeployment(null);
                }
                return Completable.complete();
            })
        );
        RxVertxHandler.handleSession(update, resultHandler);
    }

    @Override
    public void updateClusterResource(String clusterName, K8sMonitoringData data,
            Handler<AsyncResult<K8sMonitoringData>> resultHandler) {
        K8sResourceUpdateUtility updateUtility = new K8sResourceUpdateUtility();
        Single<K8sMonitoringData> updateClusterResource = smProvider.withTransactionSingle(sm -> repository
            .findClusterByName(sm, clusterName)
            // TODO: swap with NotFoundException and handle in Monitoring Verticle
            .switchIfEmpty(Maybe.error(new MonitoringException("cluster " + clusterName + " is not registered")))
            .flatMapCompletable(cluster -> {
                data.setResourceId(cluster.getResourceId());
                if (!data.getIsUp()) {
                    return Completable.complete();
                }
                return updateUtility.updateClusterNodes(sm, cluster, data);
            })
            .toSingle(() -> data)
        );
        RxVertxHandler.handleSession(updateClusterResource, resultHandler);
    }

    @Override
    public void unlockLockedResourcesByDeploymentId(long deploymentId, Handler<AsyncResult<Void>> resultHandler) {
        LockedResourcesUtility lockUtility = new LockedResourcesUtility(repository);
        Completable unlockResources = smProvider.withTransactionCompletable(sm -> lockUtility
            .unlockDeploymentResources(sm, deploymentId)
        );
        RxVertxHandler.handleSession(unlockResources, resultHandler);
    }

    private Resource getResourceLockState(Resource resource) {
        resource.setIsLocked(resource.getLockedByDeployment() != null);
        return resource;
    }

    /**
     * Compose a JsonArray from the resourceList.
     *
     * @param resourceList the resource list
     * @return the new JsonArray
     */
    protected JsonArray mapResourceListToJsonArray(List<Resource> resourceList) {
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
