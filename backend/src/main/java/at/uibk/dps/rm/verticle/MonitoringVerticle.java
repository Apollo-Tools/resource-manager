package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringService;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringServiceImpl;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MonitoringVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringVerticle.class);

    ServiceProxyProvider serviceProxyProvider;

    @Override
    public Completable rxStart() {
        serviceProxyProvider = new ServiceProxyProvider(vertx);
        return new ConfigUtility(vertx).getConfigDTO().map(config ->
            vertx.executeBlocking(fut -> fut.complete(monitorK8s(config)))
                .map(monitoringData -> {
                    // Update / create clusters

                    // Update / create nodes

                    // Update namespaces

                    return monitoringData;
                })
                .doOnError(throwable -> {
                    if (throwable instanceof MonitoringException) {
                        logger.error(throwable.getMessage());
                    } else {
                        throw new RuntimeException(throwable);
                    }
                })
                .onErrorComplete()
                .ignoreElement()
                .subscribe())
        .ignoreElement();
    }

    private Map<String, K8sMonitoringData> monitorK8s(ConfigDTO config) {
        try {
            Map<String, K8sMonitoringData> monitoringDataMap = new HashMap<>();
            K8sMonitoringService monitoringService = new K8sMonitoringServiceImpl();
            Map<String, String> kubeConfigs = monitoringService.listSecrets(config);
            for (Map.Entry<String, String> entry: kubeConfigs.entrySet()) {
                logger.info(entry.getKey());
                ApiClient externalClient = Config.fromConfig(new StringReader(entry.getValue()));
                Configuration.setDefaultApiClient(externalClient);
                try {
                    List<K8sNode> nodes = monitoringService.listNodes(entry.getValue(), config);
                    List<V1Namespace> namespaces = monitoringService.listNamespaces(entry.getValue(), config);
                    for (K8sNode node: nodes) {
                        monitoringService.getCurrentNodeAllocation(node, entry.getValue(), config);
                    }
                    K8sMonitoringData k8sMonitoringData = new K8sMonitoringData(nodes, namespaces);
                    monitoringDataMap.put(entry.getKey(), k8sMonitoringData);
                } catch (ApiException ex) {
                    logger.warn("connection for config " + entry.getKey() + " failed");
                }
            }
            return monitoringDataMap;
        } catch (IOException e) {
            throw new MonitoringException();
        }
    }
}
