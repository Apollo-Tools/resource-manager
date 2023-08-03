package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MonitoringVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringVerticle.class);

    @Override
    public Completable rxStart() {
        return new ConfigUtility(vertx).getConfigDTO().map(config ->
            vertx.executeBlocking(fut -> {
                monitorK8s(config);
                fut.complete();
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

    private void monitorK8s(ConfigDTO config) {
        try {
            ApiClient localClient = Config.defaultClient();
            Configuration.setDefaultApiClient(localClient);
            CoreV1Api api = new CoreV1Api();
            Map<String, String> kubeConfigs;
            try {
                kubeConfigs = listSecrets(api, config);
            } catch (ApiException ex) {
                throw new MonitoringException("failed to list secrets");
            }
            for (Map.Entry<String, String> entry: kubeConfigs.entrySet()) {
                logger.info(entry.getKey());
                ApiClient externalClient = Config.fromConfig(new StringReader(entry.getValue()));
                Configuration.setDefaultApiClient(externalClient);
                api = new CoreV1Api();
                try {
                    listNodes(api, config.getKubeApiTimeoutSeconds());
                    listNamespaces(api, config.getKubeApiTimeoutSeconds());
                } catch (ApiException ex) {
                    logger.warn("connection for config " + entry.getKey() + " failed");
                }
            }
        } catch (IOException e) {
            throw new MonitoringException();
        }
    }

    private void listNodes(CoreV1Api api, int timeout) throws ApiException {
        V1NodeList list = api.listNode(null, null,  null, null,
            null, null, null, null, timeout, false);
        String header = "\n############### Nodes ###############\n";
        String nodes = list.getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName())
            .collect(Collectors.joining("\n"));
        logger.info(header + nodes);
    }

    private void listNamespaces(CoreV1Api api, int timeout) throws ApiException {
        V1NamespaceList list = api.listNamespace(null, null,  null, null,
            null, null, null, null, timeout, null);
        String header = "\n############### Namespaces ###############\n";
        String nodes = list.getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName())
            .collect(Collectors.joining("\n"));
        logger.info(header + nodes);
    }

    private Map<String, String> listSecrets(CoreV1Api api, ConfigDTO config) throws ApiException {
        logger.info("############### Secrets ###############");
        V1SecretList list = api.listNamespacedSecret(config.getKubeConfigSecretsNamespace(), null,
            null, null, "metadata.name=" + config.getKubeConfigSecretsName(),
            null, null, null,null, config.getKubeApiTimeoutSeconds(),
            null);
        if (list.getItems().isEmpty() || list.getItems().get(0).getData() == null){
            logger.info("no secrets found");
            return new HashMap<>();
        }
        V1Secret item = list.getItems().get(0);
        Map<String, String> configs = new HashMap<>();
        for (Map.Entry<String, byte[]> entry: Objects.requireNonNull(item.getData()).entrySet()) {
            String kubeConfig = new String(entry.getValue(), StandardCharsets.UTF_8);
            configs.put(entry.getKey(), kubeConfig);
        }
        return configs;
    }



}
