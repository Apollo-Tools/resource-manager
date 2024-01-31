package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.util.misc.MultiValuedMapCollector;
import at.uibk.dps.rm.verticle.MonitoringVerticle;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.apache.commons.collections4.MultiValuedMap;

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

    private CoreV1Api setUpLocalClient() {
        try {
            ApiClient localClient = Config.defaultClient();
            Configuration.setDefaultApiClient(localClient);
            return new CoreV1Api();
        } catch (IOException e) {
            throw new MonitoringException();
        }
    }

    private CoreV1Api setUpExternalClient(Path kubeConfig) {
        try {
            ApiClient externalClient = Config.fromConfig(kubeConfig.toAbsolutePath().toString());
            Configuration.setDefaultApiClient(externalClient);
            return new CoreV1Api();
        } catch (IOException e) {
            throw new MonitoringException();
        }
    }

    @Override
    public Map<String, String> listSecrets(ConfigDTO config) {
        try {
            CoreV1Api api = setUpLocalClient();
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
    public List<V1Namespace> listNamespaces(Path kubeConfig, ConfigDTO config) {
        try {
            CoreV1Api api = setUpExternalClient(kubeConfig);
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
    public List<K8sNode> listNodes(Path kubeConfig, ConfigDTO config) {
        try {
            CoreV1Api api = setUpExternalClient(kubeConfig);
            V1NodeList list = api.listNode(null, null,  null, null,
                null, null, null, null, config.getKubeApiTimeoutSeconds(),
                false);
            String header = "\n############### Nodes ###############\n";
            String nodes = list.getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName())
                .collect(Collectors.joining("\n"));
            Map<String, Map<String, Quantity>> nodeMetricsMap = getCurrentNodeAllocation();
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
    public MultiValuedMap<String, K8sPod> getCurrentPodAllocation(String namespace) {
        List<PodMetrics> podMetricsList;
        try {
            podMetricsList = new Metrics().getPodMetrics(namespace).getItems();
        } catch (ApiException ex) {
            logger.error("Scrape k8s pods: " + ex.getMessage());
            throw new MonitoringException("failed to scrape pod metrics");
        }
        return podMetricsList.stream()
            .filter(podMetrics -> {
                Map<String, String> labels = podMetrics.getMetadata().getLabels() == null ? Map.of() :
                    podMetrics.getMetadata().getLabels();
                return labels.containsKey("source") && labels.containsKey("apollo-type") &&
                    labels.get("source").equals("apollo-rm-deployment") && labels.get("apollo-type").equals("pod");
            })
            .collect(new MultiValuedMapCollector<>(podMetrics -> podMetrics.getMetadata().getLabels().get("deployment"),
                podMetrics -> {
                    Map<String, String> labels = podMetrics.getMetadata().getLabels();
                    K8sPod k8sPod = new K8sPod();
                    k8sPod.setDeploymentId(Long.parseLong(labels.get("deployment")));
                    k8sPod.setResourceDeploymentId(Long.parseLong(labels.get("resource-deployment")));
                    k8sPod.setCpuLoad(podMetrics.getContainers().stream()
                        .map(container -> container.getUsage().get("cpu"))
                        .reduce((a, b) -> new Quantity(a.getNumber().add(b.getNumber()), a.getFormat()))
                        .orElse(new Quantity("0")));
                    k8sPod.setMemoryLoad(podMetrics.getContainers().stream()
                        .map(container -> container.getUsage().get("memory"))
                        .reduce((a, b) -> new Quantity(a.getNumber().add(b.getNumber()), a.getFormat()))
                        .orElse(new Quantity("0")));
                    return k8sPod;
                }));
    }

    private Map<String, Map<String, Quantity>> getCurrentNodeAllocation() throws ApiException {
        List<NodeMetrics> nodeMetricsList = new Metrics().getNodeMetrics().getItems();
        return new HashMap<>(nodeMetricsList.stream().collect(
            Collectors.toMap(nodeMetrics -> nodeMetrics.getMetadata().getName(), NodeMetrics::getUsage)));
    }
}

