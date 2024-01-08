package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringService;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringServiceImpl;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.util.Config;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    @Override
    public void startMonitoringLoop() {
        pauseLoop = false;
        long period = (long) (configDTO.getKubeMonitoringPeriod() * 60 * 1000);
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        monitoringHandler = id -> {
            logger.info("Started: monitor k8s resources");
            vertx.executeBlocking(fut -> fut.complete(monitorK8s()))
                .map(monitoringData -> (Map<String, K8sMonitoringData>) monitoringData)
                .flatMapObservable(monitoringData -> Observable.fromIterable(monitoringData.entrySet()))
                .toList()
                .flatMapCompletable(entries -> {
                    // Necessary to prevent serialization error
                    Completable completable = Completable.complete();
                    for (Map.Entry<String, K8sMonitoringData> entry : entries) {
                        List<String> namespaces = entry.getValue().getNamespaces().stream()
                            .map(namespace -> Objects.requireNonNull(namespace.getMetadata()).getName())
                            .collect(Collectors.toList());
                        completable =
                            completable.andThen(Completable.defer(() -> serviceProxyProvider.getResourceService()
                            .updateClusterResource(entry.getKey(), entry.getValue())))
                            .andThen(Completable.defer(() ->
                                    serviceProxyProvider.getNamespaceService().updateAllClusterNamespaces(entry.getKey(),
                                namespaces)))
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

    // TODO: parallelize monitoring of multiple clusters, retrieve configs from k8s secret volume
    private Map<String, K8sMonitoringData> monitorK8s() {
        try {
            FileSystem fileSystem = vertx.fileSystem();
            Map<String, K8sMonitoringData> monitoringDataMap = new HashMap<>();
            K8sMonitoringService monitoringService = new K8sMonitoringServiceImpl();
            Map<String, String> kubeConfigs = monitoringService.listSecrets(configDTO);
            if (!fileSystem.existsBlocking(configDTO.getKubeConfigDirectory())) {
                fileSystem.mkdirsBlocking(configDTO.getKubeConfigDirectory());
            }
            for (Map.Entry<String, String> entry: kubeConfigs.entrySet()) {
                logger.info("Observe cluster: " + entry.getKey());
                Path kubeconfigPath = Path.of(configDTO.getKubeConfigDirectory(), entry.getKey());
                fileSystem.writeFileBlocking(kubeconfigPath.toString(), Buffer.buffer(entry.getValue()));
                ApiClient externalClient = Config.fromConfig(kubeconfigPath.toAbsolutePath().toString());
                Configuration.setDefaultApiClient(externalClient);
                List<K8sNode> nodes = monitoringService.listNodes(kubeconfigPath, configDTO);
                List<V1Namespace> namespaces = monitoringService.listNamespaces(kubeconfigPath, configDTO);
                K8sMonitoringData k8sMonitoringData = new K8sMonitoringData(nodes, namespaces);
                monitoringDataMap.put(entry.getKey(), k8sMonitoringData);
            }
            return monitoringDataMap;
        } catch (IOException e) {
            throw new MonitoringException("failed to setup config");
        }
    }
}
