package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.testutil.mockprovider.K8sObjectMockprovider;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestK8sProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.NodeMetricsList;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link K8sMonitoringServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class K8sMonitoringServiceImplTest {

    private K8sMonitoringService monitoringService;

    @Mock
    private ApiClient apiClient;

    private V1SecretList secretList;
    private V1Secret s1;
    private V1NamespaceList namespaceList;
    private V1Namespace ns1, ns2;
    private V1NodeList nodeList;
    private V1Node node1, node2;
    private ConfigDTO config;
    private Path kubeConfigPath;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        monitoringService = new K8sMonitoringServiceImpl();
        config = TestConfigProvider.getConfigDTO();
        s1 = TestK8sProvider.createSecret();
        secretList = new V1SecretList();
        namespaceList = new V1NamespaceList();
        ns1 = TestK8sProvider.createNamespace("namespace1");
        ns2 = TestK8sProvider.createNamespace("namespace2");
        nodeList = new V1NodeList();
        node1 = TestK8sProvider.createNode("node1");
        node2 = TestK8sProvider.createNode("node2");
        kubeConfigPath = Path.of("path", "to", "config");
    }

    @Test
    void listSecrets() {
        secretList.setItems(List.of(s1));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider
                    .mockCoreV1ApiListNamedSecrets(config, secretList)) {
            k8sConfig.when(Config::defaultClient).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            Map<String, String> configs = monitoringService.listSecrets(apiClient, config);
            assertThat(configs.size()).isEqualTo(2);
            assertThat(configs.get("cluster1")).isEqualTo("secret-value");
            assertThat(configs.get("cluster2")).isEqualTo("value-secret");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"empty", "nullData"})
    void listSecretsNotFound(String type) {
        if (type.equals("empty")) {
            secretList.setItems(List.of());
        } else {
            s1.setData(null);
            secretList.setItems(List.of(s1));
        }

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider
                    .mockCoreV1ApiListNamedSecrets(config, secretList)) {
            k8sConfig.when(Config::defaultClient).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            Map<String, String> configs = monitoringService.listSecrets(apiClient, config);
            assertThat(configs.size()).isEqualTo(0);
        }
    }

    @Test
    void listSecretsApiException() {
        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider.mockCoreV1ApiListNamedSecretsException(config)) {
            k8sConfig.when(Config::defaultClient).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            MonitoringException exception = assertThrows(MonitoringException.class, () ->
                monitoringService.listSecrets(apiClient, config));
            assertThat(exception.getMessage()).isEqualTo("failed to list secrets");
        }
    }

    @Test
    void listNamespaces() {
        namespaceList.setItems(List.of(ns1, ns2));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider
                    .mockCoreV1ApiListNamespaces(config, namespaceList)) {
            k8sConfig.when(() -> Config.fromConfig(kubeConfigPath.toAbsolutePath().toString())).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            List<V1Namespace> namespaces = monitoringService.listNamespaces(apiClient, config);
            assertThat(namespaces.size()).isEqualTo(2);
            assertThat(namespaces.get(0)).isEqualTo(ns1);
            assertThat(namespaces.get(1)).isEqualTo(ns2);
        }
    }

    @Test
    void listNamespacesApiException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
            MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
            MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider.mockCoreV1ApiListNamespacesException(config)) {
            k8sConfig.when(() -> Config.fromConfig(kubeConfigPath.toAbsolutePath().toString())).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            MonitoringException exception = assertThrows(MonitoringException.class, () ->
                monitoringService.listNamespaces(apiClient, config));
            assertThat(exception.getMessage()).isEqualTo("failed to list namespaces");
        }
    }

    @Test
    void listNodes() {
        nodeList.setItems(List.of(node1, node2));
        NodeMetrics nm1 = TestK8sProvider.createNodeMetrics("node1", 3.25, 45000200);
        NodeMetrics nm2 = TestK8sProvider.createNodeMetrics("node2", 1.2, 34000200);
        NodeMetricsList nodeMetricsList = new NodeMetricsList();
        nodeMetricsList.setItems(List.of(nm1, nm2));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignoreApi = K8sObjectMockprovider
                    .mockCoreV1ApiListNodes(config, nodeList);
                MockedConstruction<Metrics> ignoreMetrics = K8sObjectMockprovider
                    .mockMetricsNodeUtilisation(nodeMetricsList)) {
            k8sConfig.when(() -> Config.fromConfig(kubeConfigPath.toAbsolutePath().toString())).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            List<K8sNode> nodes = monitoringService.listNodes(apiClient, config);
            assertThat(nodes.size()).isEqualTo(2);
            assertThat(nodes.get(0).getNode()).isEqualTo(node1);
            assertThat(nodes.get(0).getCpuLoad().getNumber().doubleValue()).isEqualTo(3.25);
            assertThat(nodes.get(0).getMemoryLoad().getNumber().doubleValue()).isEqualTo(45000200);
            assertThat(nodes.get(1).getNode()).isEqualTo(node2);
            assertThat(nodes.get(1).getCpuLoad().getNumber().doubleValue()).isEqualTo(1.2);
            assertThat(nodes.get(1).getMemoryLoad().getNumber().doubleValue()).isEqualTo(34000200);
        }
    }

    @Test
    void listNodesException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
            MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
            MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider.mockCoreV1ApiListNodesException(config)) {
            k8sConfig.when(() -> Config.fromConfig(kubeConfigPath.toAbsolutePath().toString())).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            MonitoringException exception = assertThrows(MonitoringException.class, () ->
                monitoringService.listNodes(apiClient, config));
            assertThat(exception.getMessage()).isEqualTo("failed to list nodes");
        }
    }

    // TODO: add tests

    @Test
    void setupLocalClientIOException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class)) {
            k8sConfig.when(Config::defaultClient).thenThrow(IOException.class);

            assertThrows(MonitoringException.class, () -> monitoringService.listSecrets(apiClient, config));
        }
    }

    @Test
    void setupExternalClientIOException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedConstruction<CoreV1Api> ignore = K8sObjectMockprovider
                    .mockCoreV1ApiListNamespaces(config, namespaceList)) {
            k8sConfig.when(() -> Config.fromConfig(kubeConfigPath.toAbsolutePath().toString()))
                .thenThrow(IOException.class);

            assertThrows(MonitoringException.class, () -> monitoringService.listNamespaces(apiClient, config));
        }
    }
}
