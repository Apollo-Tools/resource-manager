package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringService;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringServiceImpl;
import at.uibk.dps.rm.util.monitoring.LatencyMonitoringUtility;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.util.Config;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This monitoring handler monitors k8s clusters. This includes checking if new nodes got added,
 * existing nodes got deleted and the current usage of cpu, memory and storage of each node.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class K8sMonitoringHandler implements MonitoringHandler {
    private static final Logger logger = LoggerFactory.getLogger(K8sMonitoringHandler.class);

    private final Vertx vertx;
    
    private final ConfigDTO configDTO;

    private long currentTimer = -1L;

    private boolean pauseLoop = false;

    private static Handler<Long> monitoringHandler;

    private final K8sMonitoringService monitoringService = new K8sMonitoringServiceImpl();
    
    @Override
    public void startMonitoringLoop() {
        pauseLoop = false;
        long period = (long) (configDTO.getKubeMonitoringPeriod() * 60 * 1000);
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        monitoringHandler = id -> {
            logger.info("Started: monitor k8s resources");
            monitorK8s()
                .flatMapCompletable(entries -> {
                    // Necessary to prevent serialization error
                    Completable completable = Completable.complete();
                    for (K8sMonitoringData entry : entries) {
                        List<String> namespaces = entry.getNamespaces().stream()
                            .map(namespace -> Objects.requireNonNull(namespace.getMetadata()).getName())
                            .collect(Collectors.toList());
                        completable =
                            completable.andThen(Single.defer(() -> serviceProxyProvider.getResourceService()
                                .updateClusterResource(entry.getName(), entry))
                            .flatMapCompletable(updatedMonitoringData ->
                                serviceProxyProvider.getNamespaceService()
                                    .updateAllClusterNamespaces(entry.getName(), namespaces)
                                .andThen(serviceProxyProvider.getK8sMetricPushService()
                                    .composeAndPushMetrics(updatedMonitoringData))
                            ))
                            .doOnError(throwable -> {
                                logger.error(throwable.getMessage());
                                if (!(throwable instanceof MonitoringException)) {
                                    throw new RuntimeException(throwable);
                                }
                            });
                    }
                    return completable;
                }).subscribe(() -> {
                    logger.info("Finished: monitor k8s resources");
                    currentTimer = pauseLoop ? currentTimer : vertx.setTimer(period, monitoringHandler);
                }, throwable -> {
                    logger.error(throwable.getMessage());
                    currentTimer = pauseLoop ? currentTimer : vertx.setTimer(period, monitoringHandler);
                });
        };
        currentTimer = vertx.setTimer(period, monitoringHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        pauseLoop = true;
        if (!vertx.cancelTimer(currentTimer)) {
            vertx.cancelTimer(currentTimer);
        }
        currentTimer = -1L;
    }

    // TODO: retrieve configs from k8s secret volume
    /**
     * Collect existing nodes, namespaces, pods and metrics for all registered k8s clusters.
     *
     * @return a Single that emits a list of the resulting monitoring data for all registered
     * k8s clusters.
     */
    private Single<List<K8sMonitoringData>> monitorK8s() {
    FileSystem fileSystem = vertx.fileSystem();
    K8sMonitoringService monitoringService = new K8sMonitoringServiceImpl();
    Map<String, String> kubeConfigs = monitoringService.listSecrets(configDTO);
    if (!fileSystem.existsBlocking(configDTO.getKubeConfigDirectory())) {
        fileSystem.mkdirsBlocking(configDTO.getKubeConfigDirectory());
    }
    return Observable.fromIterable(kubeConfigs.entrySet())
        .flatMapSingle(entry -> vertx.executeBlocking(fut ->
                fut.complete(observeK8sAPI(entry.getKey(), entry.getValue())))
            .switchIfEmpty(Single.just(new K8sMonitoringData(entry.getKey(), "", -1L, List.of(),
                List.of(), false, null)))
        )
        .map(monitoringData -> (K8sMonitoringData) monitoringData)
        .flatMapSingle(monitoringData -> {
            if (monitoringData.getBasePath() == null) {
                return Single.just(monitoringData);
            }
            String pingUrl = LatencyMonitoringUtility
                .getPingUrl(monitoringData.getBasePath());
            return LatencyMonitoringUtility.measureLatency(5, pingUrl)
                .map(processOutput -> {
                    if (processOutput.getProcess().exitValue() == 0) {
                        monitoringData.setLatencySeconds(Double.parseDouble(processOutput.getOutput()) / 1000);
                    } else {
                        logger.info("K8s " + monitoringData.getBasePath() + " not reachable: " +
                            processOutput.getOutput());
                    }
                    return monitoringData;
                });
        })
        .toList();
    }

    /**
     * Collect existing nodes, namespaces, pods and metrics for the cluster with clusterName.
     *
     * @param clusterName the name of the cluster
     * @param kubeConfig the kube config for the cluster
     * @return the resulting monitoring data
     */
    private K8sMonitoringData observeK8sAPI(String clusterName, String kubeConfig) {
        FileSystem fileSystem = vertx.fileSystem();
        K8sMonitoringData k8sMonitoringData;
        try {
            logger.info("Observe cluster: " + clusterName);
            Path kubeconfigPath = Path.of(configDTO.getKubeConfigDirectory(), clusterName);
            fileSystem.writeFileBlocking(kubeconfigPath.toString(), Buffer.buffer(kubeConfig));
            ApiClient externalClient = Config.fromConfig(kubeconfigPath.toAbsolutePath().toString());
            Configuration.setDefaultApiClient(externalClient);
            List<K8sNode> nodes = monitoringService.listNodes(kubeconfigPath, configDTO);
            List<V1Namespace> namespaces = monitoringService.listNamespaces(kubeconfigPath, configDTO);
            Map<String, PodMetrics> podMetricsMap = monitoringService.getCurrentPodUtilisation(kubeconfigPath,
                configDTO);
            nodes.forEach(node -> {
                List<K8sPod> pods = monitoringService.listPodsByNode(node.getName(), kubeconfigPath, configDTO);
                pods.forEach(pod -> {
                    PodMetrics podMetrics = podMetricsMap.get(pod.getName());
                    if (podMetrics == null) {
                        return;
                    }
                    pod.setCpuLoad(podMetrics.getContainers().stream()
                        .map(container -> container.getUsage().get("cpu"))
                        .reduce((a, b) -> new Quantity(a.getNumber().add(b.getNumber()), a.getFormat()))
                        .orElse(new Quantity("0")));
                    pod.setMemoryLoad(podMetrics.getContainers().stream()
                        .map(container -> container.getUsage().get("memory"))
                        .reduce((a, b) -> new Quantity(a.getNumber().add(b.getNumber()), a.getFormat()))
                        .orElse(new Quantity("0")));
                });
                node.addAllPods(pods);
            });
            k8sMonitoringData = new K8sMonitoringData(clusterName, externalClient.getBasePath(), -1L,
                nodes, namespaces,  true, null);
        } catch (MonitoringException | IOException ex) {
            k8sMonitoringData = new K8sMonitoringData(clusterName, "", -1L, List.of(), List.of(), false, null);
        }
        return k8sMonitoringData;
    }
}
