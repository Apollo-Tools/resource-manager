package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import io.kubernetes.client.openapi.models.V1Namespace;

import java.util.List;
import java.util.Map;

/**
 * The interface of the monitoring service to collect various data from k8s resources.
 *
 * @author matthi-g
 */
public interface K8sMonitoringService {
    /**
     * List all secrets from the local context.
     *
     * @param config the vertx config
     * @return a Map with the cluster name as key and secret as value
     */
    Map<String, String> listSecrets(ConfigDTO config);

    /**
     * List all namespace of a k8s resource.
     *
     * @param kubeConfig the kube config for access to the k8s resource
     * @param config the vertx config
     * @return a List of all found namespaces
     */
    List<V1Namespace> listNamespaces(String kubeConfig, ConfigDTO config);

    /**
     * List all nodes of a k8s resource.
     *
     * @param kubeConfig the kube config for access to the k8s resource
     * @param config the vertx config
     * @return a List of all found nodes
     */
    List<K8sNode> listNodes(String kubeConfig, ConfigDTO config);

    /**
     * Get the current resource allocation by node.
     *
     * @param node the node
     * @param kubeConfig the kube config for access to the k8s resource
     * @param config the vertx config
     */
    void getCurrentNodeAllocation(K8sNode node, String kubeConfig, ConfigDTO config);
}