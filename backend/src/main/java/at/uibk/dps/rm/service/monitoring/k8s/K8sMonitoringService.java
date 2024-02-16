package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.openapi.models.V1Namespace;

import java.nio.file.Path;
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
     * @param kubeConfigPath the path to kube config to access to the k8s resource
     * @param config the vertx config
     * @return a List of all found namespaces
     */
    List<V1Namespace> listNamespaces(Path kubeConfigPath, ConfigDTO config);

    /**
     * List all nodes of a k8s resource.
     *
     * @param kubeConfigPath the path to kube config to access to the k8s resource
     * @param config the vertx config
     * @return a List of all found nodes
     */
    List<K8sNode> listNodes(Path kubeConfigPath, ConfigDTO config);

    /**
     * List all pods of a node of a k8s resource.
     *
     * @param nodeName the name of the node
     * @param kubeConfigPath the path to kube config to access to the k8s resource
     * @param config the vertx config
     * @return a List of all found pods
     */
    List<K8sPod> listPodsByNode(String nodeName, Path kubeConfigPath, ConfigDTO config);

    /**
     * Get the current resource utilisation all pods of a k8s resource.
     *
     * @param kubeConfigPath the path to kube config to access to the k8s resource
     * @param config the vertx config
     * @return a Map where the key is the name of the node where the pod is running and the value
     * are the pod metrics
     */
    Map<String, PodMetrics> getCurrentPodUtilisation(Path kubeConfigPath, ConfigDTO config);
}
