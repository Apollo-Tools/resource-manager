package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.K8sEntityData;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.exception.MonitoringException;
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
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    @Override
    public Future<Void> delete(long id) {
        CompletionStage<Void> delete = withTransaction(session -> repository.findByIdAndFetch(session, id)
            .thenCompose(entity -> {
                ServiceResultValidator.checkFound(entity, Resource.class);
                return session.remove(entity);
            })
        );
        return sessionToFuture(delete);
    }

    @Override
    public Future<Void> updateClusterResource(String clusterName, K8sMonitoringData data) {
        CompletionStage<Void> updateClusterResource = withTransaction(session ->
            repository.findClusterByName(session, clusterName)
                .thenCompose(cluster -> {
                    if (cluster != null) {
                        return updateClusterNodes(session, cluster, data)
                            .thenCompose(res -> updateCluster(session, cluster, data));
                    } else {
                        // Log cluster not found
                        throw new MonitoringException("cluster " + clusterName + " is not registered");
                    }
                })
                .thenAccept(res -> {}));
        return sessionToFuture(updateClusterResource);
    }

    private CompletionStage<Void> updateClusterNodes(Session session, MainResource cluster, K8sMonitoringData data) {
        Map<String, List<MetricValue>> mvToPersist = new HashMap<>();
        Set<SubResource> subResources =  Set.copyOf(cluster.getSubResources());
        List<SubResource> deleteNodes = new ArrayList<>();
        for(SubResource subResource : subResources) {
            Optional<K8sNode> matchingNode = data.getNodes().stream()
                .filter(node -> node.getName().equals(subResource.getName()))
                .findFirst();
            if (matchingNode.isPresent()) {
                K8sNode node = matchingNode.get();
                updateExistingMetricValues(subResource.getMetricValues(), node);
                Arrays.stream(K8sMonitoringMetricEnum.values())
                    .filter(metric -> subResource.getMetricValues().stream()
                        .noneMatch(mv -> metric.getName().equals(mv.getMetric().getMetric())))
                    .filter(K8sMonitoringMetricEnum::getIsSubResourceMetric)
                    .forEach(missingMetric -> createNewMetricValue(subResource, missingMetric, node,
                        mvToPersist));
            } else {
                deleteNodes.add(subResource);
            }
        }
        Object[] newNodes = data.getNodes().stream()
            .filter(node -> subResources.stream().noneMatch(subResource ->
                subResource.getName().equals(node.getName())))
            .map(node -> {
                SubResource subResource = new SubResource();
                subResource.setMainResource(cluster);
                subResource.setName(node.getName());
                subResource.setMetricValues(Set.of());
                Arrays.stream(K8sMonitoringMetricEnum.values())
                    .filter(K8sMonitoringMetricEnum::getIsSubResourceMetric)
                    .forEach(metric -> createNewMetricValue(subResource, metric, node, mvToPersist));
                return subResource;
            })
            .toArray();
        return session.remove(deleteNodes.toArray())
            .thenCompose(res -> session.persist(newNodes))
            .thenCompose(res -> persistMetricValues(session, mvToPersist));
    }

    private CompletionStage<Void> updateCluster(Session session, MainResource cluster, K8sMonitoringData data) {
        Map<String, List<MetricValue>> mvToPersist = new HashMap<>();
        updateExistingMetricValues(cluster.getMetricValues(), data);
        composeMissingMetricValues(cluster, data, mvToPersist);

        return persistMetricValues(session, mvToPersist);
    }

    private void updateExistingMetricValues(Set<MetricValue> metricValues, K8sEntityData entityData) {
        metricValues.forEach(metricValue -> {
            K8sMonitoringMetricEnum metric =
                K8sMonitoringMetricEnum.fromMetric(metricValue.getMetric());
            if (metric != null) {
                setMetricValue(metricValue, entityData, metric);
            }
        });
    }

    private void composeMissingMetricValues(Resource resource, K8sEntityData entityData,
            Map<String, List<MetricValue>> mvToPersist) {
        boolean isMainResources = resource instanceof MainResource;
        Arrays.stream(K8sMonitoringMetricEnum.values())
            .filter(metric -> resource.getMetricValues().stream()
                .noneMatch(mv -> metric.getName().equals(mv.getMetric().getMetric())))
            .filter(metric -> (isMainResources && metric.getIsMainResourceMetric()) ||
                (!isMainResources && metric.getIsSubResourceMetric()))
            .forEach(missingMetric -> createNewMetricValue(resource, missingMetric, entityData,
                mvToPersist));
    }

    private void createNewMetricValue(Resource resource, K8sMonitoringMetricEnum metric, K8sEntityData entityData,
            Map<String, List<MetricValue>> mvToPersist) {
        MetricValue metricValue = new MetricValue();
        metricValue.setResource(resource);
        setMetricValue(metricValue, entityData, metric);
        mvToPersist.putIfAbsent(metric.getName(), new ArrayList<>());
        mvToPersist.get(metric.getName()).add(metricValue);
    }

    private void setMetricValue(MetricValue metricValue, K8sEntityData entityData, K8sMonitoringMetricEnum metric) {
        switch (metric) {
            case HOSTNAME:
                if (entityData instanceof K8sNode) {
                    metricValue.setValue(((K8sNode) entityData).getHostname());
                }
                break;
            case CPU:
                metricValue.setValue(entityData.getTotalCPU().doubleValue());
                break;
            case CPU_AVAILABLE:
                metricValue.setValue(entityData.getAvailableCPU().doubleValue());
                break;
            case MEMORY_SIZE:
                metricValue.setValue(entityData.getTotalMemory().doubleValue());
                break;
            case MEMORY_SIZE_AVAILABLE:
                metricValue.setValue(entityData.getAvailableMemory().doubleValue());
                break;
            case STORAGE_SIZE:
                metricValue.setValue(entityData.getTotalStorage().doubleValue());
                break;
            case STORAGE_SIZE_AVAILABLE:
                metricValue.setValue(entityData.getAvailableStorage().doubleValue());
                break;
            default:
                break;
        }
    }

    private CompletionStage<Void> persistMetricValues(Session session, Map<String, List<MetricValue>> mvToPersist) {
        List<CompletableFuture<Void>> completables = new ArrayList<>();
        for (Map.Entry<String, List<MetricValue>> entry : mvToPersist.entrySet()) {
            CompletableFuture<Void> completable = metricRepository
                .findByMetric(session, entry.getKey())
                .thenCompose(metric -> {
                    entry.getValue().forEach(mv -> mv.setMetric(metric));
                    return session.persist(entry.getValue().toArray());
                })
                .toCompletableFuture();
            completables.add(completable);
        }
        return CompletableFuture.allOf(completables.toArray(CompletableFuture[]::new));
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
