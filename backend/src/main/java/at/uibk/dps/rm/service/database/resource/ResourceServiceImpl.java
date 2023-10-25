package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.K8sResourceUpdateUtility;
import at.uibk.dps.rm.service.database.util.MetricValueUtility;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import at.uibk.dps.rm.util.toscamapping.TOSCAFile;
import at.uibk.dps.rm.util.toscamapping.TOSCAMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
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
    private final MetricRepository metricRepository;
    private final MetricValueRepository metricValueRepository;
    private final PlatformMetricRepository platformMetricRepository;

    /**
     * Create an instance from the resourceRepository.
     *
     * @param repository the resource repository
     */
    public ResourceServiceImpl(ResourceRepository repository, RegionRepository regionRepository,
            MetricRepository metricRepository, MetricValueRepository metricValueRepository, PlatformMetricRepository platformMetricRepository, SessionManagerProvider smProvider) {
        super(repository, Resource.class, smProvider);
        this.repository = repository;
        this.regionRepository = regionRepository;
        this.metricRepository = metricRepository;
        this.metricValueRepository=metricValueRepository;
        this.platformMetricRepository=platformMetricRepository;
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Resource> findOne = smProvider.withTransactionMaybe( sm -> repository
            .findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class))));
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
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(findAll.map(this::encodeResourceList), resultHandler);
    }

    @Override
    public void findAllBySLOs(JsonObject data, Handler<AsyncResult<JsonArray>> resultHandler) {
        SLORequest sloRequest = data.mapTo(SLORequest.class);
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sm ->
            new SLOUtility(repository, metricRepository).findAndFilterResourcesBySLOs(sm, sloRequest));
        RxVertxHandler.handleSession(findAll.map(this::encodeResourceList), resultHandler);
    }

    @Override
    public void findAllSubResources(long resourceId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<SubResource>> findAll = smProvider.withTransactionSingle(sm ->
            repository.findAllSubresources(sm, resourceId));
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
    public void findAllByResourceIds(List<Long> resourceIds, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Resource>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByResourceIdsAndFetch(sm, resourceIds));
        RxVertxHandler.handleSession(findAll.map(this::encodeResourceList), resultHandler);
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        String name = data.getString("name");
        long regionId = data.getJsonObject("region").getLong("region_id");
        long platformId = data.getJsonObject("platform").getLong("platform_id");
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
                return sm.persist(resource);
            })
        );
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }

    public void saveStandardized(String data, Handler<AsyncResult<Void>> resultHandler) {
        TOSCAFile toscaFile = null;

        try {
            toscaFile = TOSCAMapper.readTOSCA(data);
            System.out.println(toscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("resource").getProperties().get("name"));
        } catch (JsonProcessingException e) {

        }
        String name = toscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("resource").getProperties().get("name").toString();
        long regionId =  (int)toscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("resource").getProperties().get("region");
        long platformId = (int) toscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("resource").getProperties().get("platform");
        MetricValueUtility metricValueUtility = new MetricValueUtility(metricValueRepository, metricRepository, platformMetricRepository);
        MainResource resource = new MainResource();
        TOSCAFile finalToscaFile = toscaFile;
        Completable save = smProvider.withTransactionCompletable(sm -> repository.findByName(sm, name)
                .flatMap(existingResource -> Maybe.<Region>error(new AlreadyExistsException(Resource.class)))
                .switchIfEmpty(regionRepository.findByRegionIdAndPlatformId(sm, regionId, platformId))
                .switchIfEmpty(Maybe.error(new NotFoundException("platform is not supported by the selected region")))
                .flatMap(region -> {
                    resource.setName(name);
                    resource.setRegion(region);
                    return sm.find(Platform.class, platformId);
                })
                .switchIfEmpty(Maybe.error(new NotFoundException(Platform.class)))
                .flatMap(platform -> {
                    resource.setPlatform(platform);
                    sm.persist(resource);
                    return repository.findByName(sm, name);
                }).switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
                .flatMapCompletable(resource1 -> {
                    System.out.println(resource1.getResourceId());
                    Map<String, Object> props = finalToscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("ec2instance").getProperties();
                    JsonArray jsonArray = new JsonArray();
                    props.entrySet().forEach(entry ->{
                        JsonObject jsonObject =new JsonObject();
                        jsonObject.put("metric", entry.getKey());
                        jsonObject.put("value", entry.getValue());
                        jsonArray.add(jsonObject);
                    });
                    return metricValueUtility.checkAddMetricList(sm,resource1,jsonArray);
                })
        );
        RxVertxHandler.handleSession(save, resultHandler);
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
    public void updateClusterResource(String clusterName, K8sMonitoringData data,
            Handler<AsyncResult<Void>> resultHandler) {
        K8sResourceUpdateUtility updateUtility = new K8sResourceUpdateUtility(metricRepository);
        Completable updateClusterResource = smProvider.withTransactionCompletable(sm -> repository
            .findClusterByName(sm, clusterName)
            // TODO: swap with NotFoundException and handle in Monitoring Verticle
            .switchIfEmpty(Maybe.error(new MonitoringException("cluster " + clusterName + " is not registered")))
            .flatMapCompletable(cluster -> updateUtility.updateClusterNodes(sm, cluster, data)
                .andThen(updateUtility.updateCluster(sm, cluster, data)))
        );
        RxVertxHandler.handleSession(updateClusterResource, resultHandler);
    }

    protected JsonArray encodeResourceList(List<Resource> resourceList) {
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
