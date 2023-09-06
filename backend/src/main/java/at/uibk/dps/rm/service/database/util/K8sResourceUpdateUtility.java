package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.SubResource;
import at.uibk.dps.rm.entity.monitoring.K8sEntityData;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * A utility class that provides various methods to update k8s cluster resources.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class K8sResourceUpdateUtility {

    private final MetricRepository metricRepository;

    /**
     * Update all sub resources (nodes) of a k8s main resource. This includes deleting non existing
     * nodes, creating unregistered nodes and updating metric values for all nodes.
     *
     * @param sessionManager the database session manager
     * @param cluster the main resource
     * @param data the monitoring data that is used to update the metric values
     * @return a Completable
     */
    public Completable updateClusterNodes(SessionManager sessionManager, MainResource cluster,
                                           K8sMonitoringData data) {
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
        return sessionManager.remove(deleteNodes.toArray())
            .andThen(sessionManager.persist(newNodes))
            .andThen(persistMetricValues(sessionManager, mvToPersist));
    }

    /**
     * Update the metric values of a cluster resource.
     *
     * @param sessionManager the database session manager
     * @param cluster the resource
     * @param data the monitoring data that is used to update the metric values
     * @return a Completable
     */
    public Completable updateCluster(SessionManager sessionManager, MainResource cluster,
            K8sMonitoringData data) {
        Map<String, List<MetricValue>> mvToPersist = new HashMap<>();
        updateExistingMetricValues(cluster.getMetricValues(), data);
        composeMissingMetricValues(cluster, data, mvToPersist);
        return persistMetricValues(sessionManager, mvToPersist);
    }

    /**
     * Update existing metric values based on k8s monitoring data.
     *
     * @param metricValues the metric values to update
     * @param entityData the monitoring data
     */
    private void updateExistingMetricValues(Set<MetricValue> metricValues, K8sEntityData entityData) {
        metricValues.forEach(metricValue -> {
            K8sMonitoringMetricEnum metric =
                K8sMonitoringMetricEnum.fromMetric(metricValue.getMetric());
            if (metric != null) {
                setMetricValue(metricValue, entityData, metric);
            }
        });
    }

    /**
     * Create new metric values from monitoring data.
     *
     * @param resource the resource to create the missing metric values for
     * @param entityData the monitoring data
     * @param mvToPersist the list where the missing metric values are added to
     */
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

    /**
     * Create a new MetricValue object for a resource, using its metric, monitoring data and add it
     * to the mvToPersist to later persist it.
     *
     * @param resource the resource
     * @param metric the metric
     * @param entityData the monitoring data
     * @param mvToPersist the map of metric values to persist
     */
    private void createNewMetricValue(Resource resource, K8sMonitoringMetricEnum metric, K8sEntityData entityData,
                                      Map<String, List<MetricValue>> mvToPersist) {
        MetricValue metricValue = new MetricValue();
        metricValue.setResource(resource);
        setMetricValue(metricValue, entityData, metric);
        mvToPersist.putIfAbsent(metric.getName(), new ArrayList<>());
        mvToPersist.get(metric.getName()).add(metricValue);
    }

    /**
     * Set a metric value base on its metric.
     *
     * @param metricValue the metric value
     * @param entityData the data to use
     * @param metric the metric
     */
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

    /**
     * Persist a map of metric values.
     *
     * @param sessionManager the database session manager
     * @param mvToPersist the metric values
     * @return a Completable
     */
    private Completable persistMetricValues(SessionManager sessionManager,
                                            Map<String, List<MetricValue>> mvToPersist) {
        List<Completable> completables = new ArrayList<>();
        for (Map.Entry<String, List<MetricValue>> entry : mvToPersist.entrySet()) {
            Completable completable = metricRepository
                .findByMetric(sessionManager, entry.getKey())
                .flatMapCompletable(metric -> {
                    entry.getValue().forEach(mv -> mv.setMetric(metric));
                    return sessionManager.persist(entry.getValue().toArray());
                });
            completables.add(completable);
        }
        return Completable.merge(completables);
    }
}
