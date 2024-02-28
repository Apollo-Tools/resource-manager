package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.verticle.MonitoringVerticle;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.custom.PodMetricsList;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.options.ListOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link K8sMonitoringService}.
 *
 * @author matthi-g
 */
public class K8sMonitoringServiceImpl implements K8sMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringVerticle.class);

    private final String POD_LABEL_SELECTOR = "source=apollo-rm-deployment,apollo-type=pod";

    public static ApiClient setUpLocalClient() {
        try {
            return Config.defaultClient();
        } catch (IOException e) {
            throw new MonitoringException();
        }
    }

    public static ApiClient setUpExternalClient(Path kubeConfig) {
        try {
            return Config.fromConfig(kubeConfig.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new MonitoringException();
        }
    }

    @Override
    public Map<String, String> listSecrets(ApiClient apiClient, ConfigDTO config) {
        try {
            CoreV1Api api = new CoreV1Api(apiClient);
            V1SecretList list = api.listNamespacedSecret(config.getKubeConfigSecretsNamespace(), null,
                null, null, "metadata.name=" + config.getKubeConfigSecretsName(),
                null, null, null,null, config.getKubeApiTimeoutSeconds(),
                null);
            if (list.getItems().isEmpty() || list.getItems().get(0).getData() == null){
                logger.warn("no secrets found");
                return new HashMap<>();
            }
            V1Secret item = list.getItems().get(0);
            Map<String, String> configs = new HashMap<>();
            for (Map.Entry<String, byte[]> entry: Objects.requireNonNull(item.getData()).entrySet()) {
                String kubeConfig = new String(entry.getValue(), StandardCharsets.UTF_8);
                configs.put(entry.getKey(), kubeConfig);
            }
            return configs;
        } catch (ApiException ex) {
            logger.warn("Failed to list secrets: " + ex.getMessage());
            throw new MonitoringException("failed to list secrets");
        }
    }

    @Override
    public List<V1Namespace> listNamespaces(ApiClient apiClient, ConfigDTO config) {
        try {
            CoreV1Api api = new CoreV1Api(apiClient);
            V1NamespaceList list = api.listNamespace(null, null,  null, null,
                    null, null, null, null, config.getKubeApiTimeoutSeconds(),
                    null);
            String header = "\n############### Namespaces ###############\n";
            String nodes = list.getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName())
                .collect(Collectors.joining("\n"));
            logger.debug(header + nodes);
            return list.getItems();
        } catch (ApiException ex) {
            logger.error("Failed to list namespaces: " + ex.getMessage());
            throw new MonitoringException("failed to list namespaces");
        }
    }

    @Override
    public List<K8sNode> listNodes(ApiClient apiClient, ConfigDTO config) {
        try {
            CoreV1Api api = new CoreV1Api(apiClient);
            V1NodeList list = api.listNode(null, null,  null, null,
                null, null, null, null, config.getKubeApiTimeoutSeconds(),
                false);
            String header = "\n############### Nodes ###############\n";
            String nodes = list.getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName())
                .collect(Collectors.joining("\n"));
            Metrics metricsClient = new Metrics(apiClient);
            Map<String, Map<String, Quantity>> nodeMetricsMap = getCurrentNodeUtilisation(metricsClient);
            logger.debug(header + nodes);
            return list.getItems()
                .stream().map(K8sNode::new)
                .peek(node -> {
                    if (nodeMetricsMap.containsKey(node.getName())) {
                        node.setCpuLoad(nodeMetricsMap.get(node.getName()).get("cpu"));
                        node.setMemoryLoad(nodeMetricsMap.get(node.getName()).get("memory"));
                        node.setStorageLoad(new Quantity("0.0"));
                    }
                })
                .collect(Collectors.toList());
        } catch (ApiException ex) {
            logger.error("Scrape k8s nodes: " + ex.getMessage());
            throw new MonitoringException("failed to list nodes");
        }
    }

    @Override
    public List<K8sPod> listPodsByNode(ApiClient apiClient, String nodeName, ConfigDTO config) {
        String fieldSelector = "spec.nodeName=" + nodeName;
        try {
            CoreV1Api api = new CoreV1Api(apiClient);
            V1PodList list = api.listPodForAllNamespaces(null, null, fieldSelector,
                POD_LABEL_SELECTOR, null, null, null, null,
                config.getKubeApiTimeoutSeconds(), false);
            String header = "\n############### Pods ###############\n";
            String pods = list.getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName())
                .collect(Collectors.joining("\n"));
            logger.debug(header + pods);
            return list.getItems()
                .stream().map(pod -> {
                    K8sPod k8sPod = new K8sPod();
                    k8sPod.setName(Objects.requireNonNull(pod.getMetadata()).getName());
                    k8sPod.setV1Pod(pod);
                    Map<String, String> labels = Objects.requireNonNull(pod.getMetadata()).getLabels();
                    k8sPod.setDeploymentId(Long.parseLong(labels.get("deployment")));
                    k8sPod.setResourceDeploymentId(Long.parseLong(labels.get("resource-deployment")));
                    k8sPod.setServiceId(Long.parseLong(labels.get("service")));
                    return k8sPod;
                })
                .collect(Collectors.toList());
        } catch (ApiException ex) {
            logger.error("Scrape k8s nodes: " + ex.getMessage());
            throw new MonitoringException("failed to list nodes");
        }
    }

    @Override
    public Map<String, PodMetrics> getCurrentPodUtilisation(ApiClient apiClient, ConfigDTO config) {
        List<PodMetrics> podMetricsList;
        try {
            CoreV1Api api = new CoreV1Api(apiClient);
            podMetricsList = getPodMetricsByNode(api.getApiClient(), config).getItems();
        } catch (ApiException ex) {
            logger.error("Scrape k8s pods: " + ex.getMessage());
            throw new MonitoringException("failed to scrape pod metrics");
        }
        return podMetricsList.stream()
            .collect(Collectors.toMap(podMetrics -> podMetrics.getMetadata().getName(),
                podMetrics -> podMetrics));
    }

    /**
     * Get the current node utilisation.
     *
     * @return a Map where the key is the name of the node and the value is the node metrics data
     * @throws ApiException if an error occurs during the retrieval of the metrics
     */
    private Map<String, Map<String, Quantity>> getCurrentNodeUtilisation(Metrics metricsClient) throws ApiException {
        List<NodeMetrics> nodeMetricsList =metricsClient.getNodeMetrics().getItems();
        return new HashMap<>(nodeMetricsList.stream().collect(
            Collectors.toMap(nodeMetrics -> nodeMetrics.getMetadata().getName(), NodeMetrics::getUsage)));
    }

    /**
     * Get the current pod utilisation by node.
     *
     * @param apiClient the k8s client
     * @param config the vertx config
     * @return a list of pod metrics
     * @throws ApiException if an error occurs during the retrieval of the metrics
     */
    private PodMetricsList getPodMetricsByNode(ApiClient apiClient, ConfigDTO config)
            throws ApiException {
        GenericKubernetesApi<PodMetrics, PodMetricsList> metricsClient = new GenericKubernetesApi<>(PodMetrics.class,
            PodMetricsList.class, "metrics.k8s.io", "v1beta1", "pods", apiClient);
        ListOptions listOptions = new ListOptions();
        listOptions.setLabelSelector(POD_LABEL_SELECTOR);
        listOptions.setTimeoutSeconds(config.getKubeApiTimeoutSeconds());
        return metricsClient.list(listOptions).throwsApiException().getObject();
    }
}

