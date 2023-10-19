package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringService;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringServiceImpl;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * All monitoring processes of the resource manager are executed on the ApiVerticle.
 *
 * @author matthi-g
 */
public class MonitoringVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringVerticle.class);

    ServiceProxyProvider serviceProxyProvider;

    private static Handler<Long> monitoringHandler;

    @Override
    public Completable rxStart() {
        serviceProxyProvider = new ServiceProxyProvider(vertx);
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        long period = (long) (config.getKubeMonitoringPeriod() * 60 * 1000);
        monitoringHandler = id -> {
            logger.info("Started: monitor k8s resources");
            vertx.executeBlocking(fut -> fut.complete(monitorK8s(config)))
                .map(monitoringData -> (Map<String, K8sMonitoringData>) monitoringData)
                .flatMapObservable(monitoringData -> Observable.fromIterable(monitoringData.entrySet()))
                .flatMapCompletable(entry -> {
                    List<String> namespaces = entry.getValue().getNamespaces().stream()
                        .map(namespace -> Objects.requireNonNull(namespace.getMetadata()).getName())
                        .collect(Collectors.toList());
                    return serviceProxyProvider.getResourceService()
                        .updateClusterResource(entry.getKey(), entry.getValue())
                        .andThen(serviceProxyProvider.getNamespaceService().updateAllClusterNamespaces(entry.getKey(),
                            namespaces))
                        .doOnError(throwable -> {
                            logger.error(throwable.getMessage());
                            if (!(throwable instanceof MonitoringException)) {
                                throw new RuntimeException(throwable);
                            }
                        });
                }).subscribe(() -> {
                    logger.info("Finished: monitor k8s resources");
                    vertx.setTimer(period, monitoringHandler);
                }, throwable -> logger.error(throwable.getMessage()));
        };
        vertx.setTimer(period, monitoringHandler);
        return Completable.complete();
    }

    // TODO: parallelize monitoring of multiple clusters
    private Map<String, K8sMonitoringData> monitorK8s(ConfigDTO config) {
        try {
            FileSystem fileSystem = vertx.fileSystem();
            Map<String, K8sMonitoringData> monitoringDataMap = new HashMap<>();
            K8sMonitoringService monitoringService = new K8sMonitoringServiceImpl();
            Map<String, String> kubeConfigs = monitoringService.listSecrets(config);
            if (!fileSystem.existsBlocking(config.getKubeConfigDirectory())) {
                fileSystem.mkdirsBlocking(config.getKubeConfigDirectory());
            }
            vertx.fileSystem().mkdirsBlocking(config.getKubeConfigDirectory());
            for (Map.Entry<String, String> entry: kubeConfigs.entrySet()) {
                logger.info("Observe cluster: " + entry.getKey());
                Path kubeconfigPath = Path.of(config.getKubeConfigDirectory(), entry.getKey());
                fileSystem.writeFileBlocking(kubeconfigPath.toString(), Buffer.buffer(entry.getValue()));
                ApiClient externalClient = Config.fromConfig(new StringReader(entry.getValue()));
                Configuration.setDefaultApiClient(externalClient);
                List<K8sNode> nodes = monitoringService.listNodes(entry.getValue(), config);
                List<V1Namespace> namespaces = monitoringService.listNamespaces(entry.getValue(), config);
                for (K8sNode node: nodes) {
                    monitoringService.getCurrentNodeAllocation(node, entry.getValue(), config);
                }
                K8sMonitoringData k8sMonitoringData = new K8sMonitoringData(nodes, namespaces);
                monitoringDataMap.put(entry.getKey(), k8sMonitoringData);
            }
            return monitoringDataMap;
        } catch (IOException e) {
            throw new MonitoringException("failed to setup config");
        }
    }

}
