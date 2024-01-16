package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.SubResource;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
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

    /**
     * Update all sub resources (nodes) of a k8s main resource. This includes deleting non existing
     * nodes, creating unregistered nodes and updating metric values for all nodes.
     *
     * @param sm the database session manager
     * @param cluster the main resource
     * @param data the monitoring data that is used to update the metric values
     * @return a Completable
     */
    public Completable updateClusterNodes(SessionManager sm, MainResource cluster,
            K8sMonitoringData data) {
        Set<SubResource> subResources =  Set.copyOf(cluster.getSubResources());
        List<SubResource> deleteNodes = new ArrayList<>();
        for(SubResource subResource : subResources) {
            Optional<K8sNode> matchingNode = data.getNodes().stream()
                .filter(node -> node.getName().equals(subResource.getName()))
                .findFirst();
            if (matchingNode.isPresent()) {
                K8sNode node = matchingNode.get();
                node.setResourceId(subResource.getResourceId());
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
                subResource.setIsLockable(false);
                return subResource;
            })
            .toArray();
        return sm.remove(deleteNodes.toArray())
            .andThen(sm.persist(newNodes));
    }
}
