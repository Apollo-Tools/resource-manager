package at.uibk.dps.rm.handler.monitoring;


import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringServiceImpl;
import at.uibk.dps.rm.service.rxjava3.database.account.NamespaceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricpusher.K8sMetricPushService;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.monitoring.LatencyMonitoringUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Implements tests for the {@link K8sMonitoringHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class K8sMonitoringHandlerTest {

    private K8sMonitoringHandler monitoringHandler;

    private Vertx spyVertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private ResourceService resourceService;

    @Mock
    private NamespaceService namespaceService;

    @Mock
    private K8sMetricPushService k8sMetricPushService;

    @Mock
    private K8sMonitoringServiceImpl k8sMonitoringService;

    @Mock
    private LatencyMonitoringUtility latencyMonitoringUtility;

    @Mock
    private ProcessOutput processOutput;

    @Mock
    private Process process;

    @Mock
    private ApiClient localClient;

    @Mock
    private ApiClient externalClient;

    private static LogCaptor logCaptor;

    private ConfigDTO configDTO;
    private Path kubeconfigPath;
    private K8sNode node1, node2;
    private V1Namespace ns1, ns2;
    private K8sPod k8sPod1, k8sPod2, k8sPod3, k8sPod4;
    private final Map<String, PodMetrics> stringPodMetricsMap = new java.util.HashMap<>();
    private K8sMonitoringData k8sMonitoringData;

    @BeforeAll
    static void initAll() {
        logCaptor = LogCaptor.forClass(K8sMonitoringHandler.class);
    }

    @AfterAll
    static void cleanupAll() {
        logCaptor.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        configDTO = TestConfigProvider.getConfigDTO();
        spyVertx = spy(vertx);
        monitoringHandler = new K8sMonitoringHandler(spyVertx, configDTO, serviceProxyProvider,
            k8sMonitoringService, latencyMonitoringUtility);

        kubeconfigPath = Path.of(configDTO.getKubeConfigDirectory(), "resource1");
        node1 = TestK8sProvider.createK8sNode("node1", 4.0, 3.0, 100000000000L, 4004000000L, 2, 1);
        node2 = TestK8sProvider.createK8sNode("node2", 2.0, 2.0, 50000000000L, 2000000L, 3, 2);
        ns1 = TestK8sProvider.createNamespace("default");
        ns2 = TestK8sProvider.createNamespace("system");
        k8sPod1 = TestK8sProvider.createK8sPod("pod1");
        k8sPod2 = TestK8sProvider.createK8sPod("pod2");
        k8sPod3 = TestK8sProvider.createK8sPod("pod3");
        k8sPod4 = TestK8sProvider.createK8sPod("pod4");
        PodMetrics pm1 = TestK8sProvider.createPodMetrics(new BigDecimal("0.5"), new BigDecimal("4000000"));
        PodMetrics pm2 = TestK8sProvider.createPodMetrics(new BigDecimal("1.5"), new BigDecimal("4000000000"));
        PodMetrics pm3 = TestK8sProvider.createPodMetrics(new BigDecimal("2.0"), new BigDecimal("2000000"));
        stringPodMetricsMap.put("pod1", pm1);
        stringPodMetricsMap.put("pod2", pm2);
        stringPodMetricsMap.put("pod3", pm3);
        stringPodMetricsMap.put("pod4", null);
        k8sMonitoringData = TestK8sProvider.createK8sMonitoringData("resource1", "localhost:001",
            1L, List.of(node1, node2), List.of(ns1, ns2), false, 0.0);

        lenient().when(serviceProxyProvider.getResourceService()).thenReturn(resourceService);
        lenient().when(serviceProxyProvider.getK8sMetricPushService()).thenReturn(k8sMetricPushService);
        lenient().when(serviceProxyProvider.getNamespaceService()).thenReturn(namespaceService);
        lenient().when(spyVertx.fileSystem()).thenReturn(fileSystem);
    }

    @AfterEach
    void cleanupEach() {
        logCaptor.clearLogs();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void startMonitoringLoopNoSecrets(boolean k8sDirExists, VertxTestContext testContext)
            throws InterruptedException {
        mockListSecrets(k8sDirExists, Map.of());

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        assertThat(logCaptor.getInfoLogs())
            .isEqualTo(List.of("Started: monitor k8s resources", "Finished: monitor k8s resources"));
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"up", "down", "nobasepath"})
    public void startMonitoringLoopNoMonitoringData(String latencyType, VertxTestContext testContext)
            throws InterruptedException, URISyntaxException {
        mockListSecrets(true, Map.of("resource1", "kubeconfig1"));
        mockObserveK8sAPI(latencyType.equals("nobasepath") ? null : "localhost:0001", latencyType.equals("nobasepath"));
        if (!latencyType.equals("nobasepath")) {
            mockLatencyMonitoring(latencyType.equals("up"));
        }
        mockPersistence(latencyType.equals("up"), latencyType.equals("nobasepath") ? null : "localhost:0001",
            latencyType.equals("nobasepath"));

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        List<String> expectedLogs = new ArrayList<>();
        expectedLogs.add("Started: monitor k8s resources");
        expectedLogs.add("Observe cluster: resource1");
        if (latencyType.equals("down")) {
            expectedLogs.add("K8s localhost:0001 not reachable: not reachable");
        }
        expectedLogs.add("Finished: monitor k8s resources");
        assertThat(logCaptor.getInfoLogs()).isEqualTo(expectedLogs);
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopObserveAPIMonitoringException(VertxTestContext testContext)
            throws InterruptedException {
        mockListSecrets(true, Map.of("resource1", "kubeconfig1"));
        mockObserveK8sAPI(null, true);

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: monitor k8s resources",
            "Observe cluster: resource1", "Finished: monitor k8s resources"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopPersistenceException(VertxTestContext testContext)
            throws InterruptedException {
        mockListSecrets(true, Map.of("resource1", "kubeconfig1"));
        mockObserveK8sAPI(null, false);
        when(resourceService.updateClusterResource(any(), any())).thenReturn(Single.error(new NotFoundException(
            "cluster resource1 is not registered")));

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: monitor k8s resources",
            "Observe cluster: resource1", "Finished: monitor k8s resources"));
        assertThat(logCaptor.getErrorLogs()).isEqualTo(List.of("cluster resource1 is not registered"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopException(VertxTestContext testContext) throws InterruptedException {
        when(spyVertx.fileSystem()).thenReturn(fileSystem);
        when(spyVertx.executeBlocking(any())).thenReturn(Maybe.error(new MonitoringException("error")));

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        assertThat(logCaptor.getErrorLogs()).isEqualTo(List.of("error"));
        testContext.completeNow();
    }


    @Test
    public void pauseMonitoringLoop() {
        monitoringHandler.pauseMonitoringLoop();
        monitoringHandler.pauseMonitoringLoop();

        verify(spyVertx, times(2)).cancelTimer(-1L);
    }

    private void mockListSecrets(boolean k8sDirExists, Map<String, String> kubeconfigs) {
        when(k8sMonitoringService.setUpLocalClient()).thenReturn(localClient);
        when(k8sMonitoringService.listSecrets(localClient, configDTO)).thenReturn(kubeconfigs);
        when(fileSystem.existsBlocking(configDTO.getKubeConfigDirectory())).thenReturn(k8sDirExists);
        if (!k8sDirExists) {
            when(fileSystem.mkdirsBlocking(configDTO.getKubeConfigDirectory())).thenReturn(fileSystem);
        }
    }

    private void mockObserveK8sAPI(String basePath, boolean isMonitoringException) {
        when(fileSystem.writeFileBlocking(kubeconfigPath.toString(), Buffer.buffer("kubeconfig1")))
            .thenReturn(fileSystem);
        if (isMonitoringException) {
            when(k8sMonitoringService.setUpExternalClient(kubeconfigPath)).thenThrow(new MonitoringException());
        } else {
            when(k8sMonitoringService.setUpExternalClient(kubeconfigPath)).thenReturn(externalClient);
            when(k8sMonitoringService.listNodes(externalClient, configDTO)).thenReturn(List.of(node1, node2));
            when(k8sMonitoringService.listNamespaces(externalClient, configDTO)).thenReturn(List.of(ns1, ns2));
            when(k8sMonitoringService.getCurrentPodUtilisation(externalClient, configDTO)).thenReturn(stringPodMetricsMap);
            when(k8sMonitoringService.listPodsByNode(externalClient, "node1", configDTO))
                .thenReturn(List.of(k8sPod1, k8sPod2));
            when(k8sMonitoringService.listPodsByNode(externalClient, "node2", configDTO))
                .thenReturn(List.of(k8sPod3, k8sPod4));
            when(externalClient.getBasePath()).thenReturn(basePath);
        }
    }

    private void mockLatencyMonitoring(boolean isUp) throws URISyntaxException {
        when(latencyMonitoringUtility.getPingUrl("localhost:0001")).thenReturn("ping/localhost");
        when(latencyMonitoringUtility.measureLatency(2, "ping/localhost"))
            .thenReturn(Single.just(processOutput));
        when(processOutput.getProcess()).thenReturn(process);
        when(process.exitValue()).thenReturn(isUp ? 0 : 1);
        if (isUp) {
            when(processOutput.getOutput()).thenReturn("123");
        } else {
            when(processOutput.getOutput()).thenReturn("not reachable");
        }
    }

    private void mockPersistence(boolean isUp, String basePath, boolean isMonitoringException) {
        if (isMonitoringException) {
            when(resourceService.updateClusterResource(eq("resource1"),
                argThat((K8sMonitoringData monitoringData) -> monitoringData.getName().equals("resource1") &&
                    !monitoringData.getIsUp() &&
                    Objects.equals(monitoringData.getBasePath(), basePath) &&
                    monitoringData.getNodes().equals(List.of()) &&
                    monitoringData.getNamespaces().equals(List.of()) &&
                    monitoringData.getTotalCPU().equals(BigDecimal.valueOf(0)) &&
                    monitoringData.getTotalMemory().equals(BigDecimal.valueOf(0)) &&
                    monitoringData.getTotalStorage().equals(BigDecimal.valueOf(0)) &&
                    monitoringData.getCPUUsed().equals(BigDecimal.valueOf(0)) &&
                    monitoringData.getMemoryUsed().equals(BigDecimal.valueOf(0)) &&
                    monitoringData.getStorageUsed().equals(BigDecimal.valueOf(0)) &&
                    Objects.equals(monitoringData.getLatencySeconds(), null))))
                .thenReturn(Single.just(k8sMonitoringData));
        } else {
            when(resourceService.updateClusterResource(eq("resource1"),
                argThat((K8sMonitoringData monitoringData) -> monitoringData.getName().equals("resource1") &&
                    (monitoringData.getIsUp() || !isUp) &&
                    Objects.equals(monitoringData.getBasePath(), basePath) &&
                    monitoringData.getNodes().equals(List.of(node1, node2)) &&
                    monitoringData.getNamespaces().equals(List.of(ns1, ns2)) &&
                    monitoringData.getTotalCPU().equals(BigDecimal.valueOf(6.0)) &&
                    monitoringData.getTotalMemory().equals(BigDecimal.valueOf(150000000000L)) &&
                    monitoringData.getTotalStorage().equals(BigDecimal.valueOf(5L)) &&
                    monitoringData.getCPUUsed().equals(BigDecimal.valueOf(5.0)) &&
                    monitoringData.getMemoryUsed().equals(BigDecimal.valueOf(4006000000L)) &&
                    monitoringData.getStorageUsed().equals(BigDecimal.valueOf(3L)) &&
                    Objects.equals(monitoringData.getLatencySeconds(), isUp ? 0.123 : null))))
                .thenReturn(Single.just(k8sMonitoringData));
        }
        when(k8sMetricPushService.composeAndPushMetrics(k8sMonitoringData)).thenReturn(Completable.complete());
        if (isUp) {
            when(namespaceService.updateAllClusterNamespaces("resource1",
                isMonitoringException ? List.of() : List.of("default", "system"))).thenReturn(Completable.complete());
        }
    }
}
