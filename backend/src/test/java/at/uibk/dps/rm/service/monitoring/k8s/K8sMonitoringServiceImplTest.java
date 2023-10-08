package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestK8sProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestMonitoringDataProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.K8sDescribeParser;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @Mock
    private Process process;

    private V1SecretList secretList;
    private V1Secret s1;
    private V1NamespaceList namespaceList;
    private V1Namespace ns1, ns2;
    private V1NodeList nodeList;
    private V1Node node1, node2;
    private K8sNode k8sNode;
    private ConfigDTO config;
    private String kubeConfig;
    private ProcessOutput processOutput;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        monitoringService = new K8sMonitoringServiceImpl();
        kubeConfig = "kubeconfigdata";
        config = TestConfigProvider.getConfigDTO();
        s1 = TestK8sProvider.createSecret();
        secretList = new V1SecretList();
        namespaceList = new V1NamespaceList();
        ns1 = TestK8sProvider.createNamespace("namespace1");
        ns2 = TestK8sProvider.createNamespace("namespace2");
        nodeList = new V1NodeList();
        node1 = TestK8sProvider.createNode("node1");
        node2 = TestK8sProvider.createNode("node2");
        k8sNode = TestMonitoringDataProvider.createK8sNode("node1");
        processOutput = TestDTOProvider.createProcessOutput(process, "output");
    }

    @Test
    void listSecrets() {
        secretList.setItems(List.of(s1));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = Mockprovider
                    .mockCoreV1ApiListNamedSecrets(config, secretList)) {
            k8sConfig.when(Config::defaultClient).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            Map<String, String> configs = monitoringService.listSecrets(config);
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
                MockedConstruction<CoreV1Api> ignore = Mockprovider
                    .mockCoreV1ApiListNamedSecrets(config, secretList)) {
            k8sConfig.when(Config::defaultClient).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            Map<String, String> configs = monitoringService.listSecrets(config);
            assertThat(configs.size()).isEqualTo(0);
        }
    }

    @Test
    void listSecretsApiException() {
        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = Mockprovider.mockCoreV1ApiListNamedSecretsException(config)) {
            k8sConfig.when(Config::defaultClient).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            MonitoringException exception = assertThrows(MonitoringException.class, () ->
                monitoringService.listSecrets(config));
            assertThat(exception.getMessage()).isEqualTo("failed to list secrets");
        }
    }

    @Test
    void listNamespaces() {
        namespaceList.setItems(List.of(ns1, ns2));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = Mockprovider
                    .mockCoreV1ApiListNamespaces(config, namespaceList)) {
            k8sConfig.when(() -> Config.fromConfig(any(StringReader.class))).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            List<V1Namespace> namespaces = monitoringService.listNamespaces(kubeConfig, config);
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
            MockedConstruction<CoreV1Api> ignore = Mockprovider.mockCoreV1ApiListNamespacesException(config)) {
            k8sConfig.when(() -> Config.fromConfig(new StringReader(kubeConfig))).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            MonitoringException exception = assertThrows(MonitoringException.class, () ->
                monitoringService.listNamespaces(kubeConfig, config));
            assertThat(exception.getMessage()).isEqualTo("failed to list namespaces");
        }
    }

    @Test
    void listNodes() {
        nodeList.setItems(List.of(node1, node2));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
                MockedConstruction<CoreV1Api> ignore = Mockprovider.mockCoreV1ApiListNodes(config, nodeList)) {
            k8sConfig.when(() -> Config.fromConfig(new StringReader(kubeConfig))).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            List<K8sNode> nodes = monitoringService.listNodes(kubeConfig, config);
            assertThat(nodes.size()).isEqualTo(2);
            assertThat(nodes.get(0).getNode()).isEqualTo(node1);
            assertThat(nodes.get(1).getNode()).isEqualTo(node2);
        }
    }

    @Test
    void listNodesException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
            MockedStatic<Configuration> k8sConfiguration = Mockito.mockStatic(Configuration.class);
            MockedConstruction<CoreV1Api> ignore = Mockprovider.mockCoreV1ApiListNodesException(config)) {
            k8sConfig.when(() -> Config.fromConfig(new StringReader(kubeConfig))).thenReturn(apiClient);
            k8sConfiguration.when(() -> Configuration.setDefaultApiClient(apiClient)).then(invocation -> null);

            MonitoringException exception = assertThrows(MonitoringException.class, () ->
                monitoringService.listNodes(kubeConfig, config));
            assertThat(exception.getMessage()).isEqualTo("failed to list nodes");
        }
    }

    @Test
    void getCurrentNodeLocationWindows() {
        System.setProperty("os.name", "Windows");
        List<String> commands = List.of("powershell.exe", "-Command", "$tempfile = New-TemporaryFile; '" +
            kubeConfig + "' | " + "Out-File -FilePath $tempfile -Encoding UTF8; kubectl describe node " +
            k8sNode.getName() + " --kubeconfig $tempfile");

        try(MockedConstruction<ProcessExecutor> ignorePe = Mockprovider
                    .mockProcessExecutor(Paths.get("").toAbsolutePath(), processOutput, commands);
                MockedStatic<K8sDescribeParser> outputParser = Mockito.mockStatic(K8sDescribeParser.class)) {
            outputParser.when(() -> K8sDescribeParser.parseContent(processOutput.getOutput(), k8sNode))
                .then(invocation -> null);
            monitoringService.getCurrentNodeAllocation(k8sNode, kubeConfig, config);
            outputParser.verify(() -> K8sDescribeParser.parseContent(processOutput.getOutput(), k8sNode));
        }
    }

    @Test
    void getCurrentNodeLocationLinux() {
        System.setProperty("os.name", "Linux");
        List<String> commands = List.of("bash", "-c", "echo <(echo '" + kubeConfig + "') && kubectl describe node "
            + k8sNode.getName() + " --kubeconfig <(echo '" + kubeConfig + "')");

        try(MockedConstruction<ProcessExecutor> ignorePe = Mockprovider
            .mockProcessExecutor(Paths.get("").toAbsolutePath(), processOutput, commands);
            MockedStatic<K8sDescribeParser> outputParser = Mockito.mockStatic(K8sDescribeParser.class)) {
            outputParser.when(() -> K8sDescribeParser.parseContent(processOutput.getOutput(), k8sNode))
                .then(invocation -> null);

            monitoringService.getCurrentNodeAllocation(k8sNode, kubeConfig, config);

            outputParser.verify(() -> K8sDescribeParser.parseContent(processOutput.getOutput(), k8sNode));
        }
    }

    @Test
    void getCurrentNodeLocationProcessFailed() {
        System.setProperty("os.name", "Linux");
        List<String> commands = List.of("bash", "-c", "echo <(echo '" + kubeConfig + "') && kubectl describe node "
            + k8sNode.getName() + " --kubeconfig <(echo '" + kubeConfig + "')");

        try(MockedConstruction<ProcessExecutor> ignorePe = Mockprovider
            .mockProcessExecutor(Paths.get("").toAbsolutePath(), processOutput, commands)) {
            when(process.exitValue()).thenReturn(-1);

            MonitoringException exception = assertThrows(MonitoringException.class,
                () -> monitoringService.getCurrentNodeAllocation(k8sNode, kubeConfig, config));
            assertThat(exception.getMessage()).isEqualTo("Retrieving node allocation failed");
        }
    }

    @Test
    void setupLocalClientIOException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class)) {
            k8sConfig.when(Config::defaultClient).thenThrow(IOException.class);

            assertThrows(MonitoringException.class, () -> monitoringService.listSecrets(config));
        }
    }

    @Test
    void setupExternalClientIOException() {
        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
                MockedStatic<Config> k8sConfig = Mockito.mockStatic(Config.class);
                MockedConstruction<CoreV1Api> ignore = Mockprovider
                    .mockCoreV1ApiListNamespaces(config, namespaceList)) {
            k8sConfig.when(() -> Config.fromConfig(any(StringReader.class))).thenThrow(IOException.class);

            assertThrows(MonitoringException.class, () -> monitoringService.listNamespaces(kubeConfig, config));
        }
    }
}
