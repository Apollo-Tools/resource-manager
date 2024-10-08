package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.OpenFaasConnectivity;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricpusher.OpenFaasMetricPushService;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.monitoring.LatencyMonitoringUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Implements tests for the {@link OpenFaasMonitoringHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class OpenFaasMonitoringHandlerTest {

    private OpenFaasMonitoringHandler monitoringHandler;

    private Vertx spyVertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private ResourceService resourceService;

    @Mock
    private OpenFaasMetricPushService openFaasMetricPushService;

    @Mock
    private LatencyMonitoringUtility latencyMonitoringUtility;

    @Mock
    private ProcessOutput po1, po2;

    @Mock
    private Process p1, p2;

    private static LogCaptor logCaptor;

    private Resource r1, r2, r3;

    @BeforeAll
    static void initAll() {
        logCaptor = LogCaptor.forClass(OpenFaasMonitoringHandler.class);
    }

    @AfterAll
    static void cleanupAll() {
        logCaptor.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        ConfigDTO configDTO = TestConfigProvider.getConfigDTO();
        spyVertx = spy(vertx);
        monitoringHandler = new OpenFaasMonitoringHandler(spyVertx, configDTO, serviceProxyProvider,
            latencyMonitoringUtility);
        Region region = TestResourceProviderProvider.createRegion(1L, "edge");
        r1 = TestResourceProvider.createResourceOpenFaas(1L, region, "http://localhost", "user", "pw");
        r2 = TestResourceProvider.createResourceOpenFaas(2L, region, "http://127.0.0.1", "user", "pw");
        r2.setMetricValues(Set.of());
        r3 = TestResourceProvider.createResourceOpenFaas(3L, region, "http://127.0.0.1", "user", "pw");

        lenient().when(serviceProxyProvider.getResourceService()).thenReturn(resourceService);
        lenient().when(serviceProxyProvider.getOpenFaasMetricPushService()).thenReturn(openFaasMetricPushService);
        lenient().when(spyVertx.fileSystem()).thenReturn(fileSystem);
    }

    @AfterEach
    void cleanupEach() {
        logCaptor.clearLogs();
    }

    @Test
    public void startMonitoringLoopNoResources(VertxTestContext testContext) throws InterruptedException {
        when(resourceService.findAllByPlatform(PlatformEnum.OPENFAAS.getValue()))
            .thenReturn(Single.just(new JsonArray()));
        when(openFaasMetricPushService.composeAndPushMetrics(new JsonArray())).thenReturn(Completable.complete());

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: monitor openfaas resources",
            "Finished: monitor openfaas resources"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoop(VertxTestContext testContext) throws InterruptedException,
            URISyntaxException {
        JsonArray resources = new JsonArray(Json.encode(List.of(r1, r2, r3)));
        OpenFaasConnectivity ofc = TestConnectivityProvider.createOpenFaasConnectivity(1L, 0.123);
        JsonArray connectivites = new JsonArray(Json.encode(List.of(ofc)));
        when(resourceService.findAllByPlatform(PlatformEnum.OPENFAAS.getValue()))
            .thenReturn(Single.just(resources));
        when(latencyMonitoringUtility.getPingUrl("http://localhost")).thenReturn("localhost");
        when(latencyMonitoringUtility.getPingUrl("http://127.0.0.1")).thenReturn("127.0.0.1");
        when(latencyMonitoringUtility.measureLatency(2, "localhost")).thenReturn(Single.just(po1));
        when(po1.getProcess()).thenReturn(p1);
        when(p1.exitValue()).thenReturn(0);
        when(po1.getOutput()).thenReturn("123");
        when(latencyMonitoringUtility.measureLatency(2, "127.0.0.1")).thenReturn(Single.just(po2));
        when(po2.getProcess()).thenReturn(p2);
        when(p2.exitValue()).thenReturn(1);
        when(po2.getOutput()).thenReturn("offline");
        when(openFaasMetricPushService.composeAndPushMetrics(connectivites)).thenReturn(Completable.complete());

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: monitor openfaas resources",
            "Monitor latency: mainresource1", "Monitor latency: mainresource3",
            "Resource mainresource3 not reachable: offline", "Finished: monitor openfaas resources"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopError(VertxTestContext testContext) throws InterruptedException {
        when(resourceService.findAllByPlatform(PlatformEnum.OPENFAAS.getValue()))
            .thenReturn(Single.error(new BadInputException("error")));

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: monitor openfaas resources"));
        assertThat(logCaptor.getErrorLogs()).isEqualTo(List.of("error"));
        testContext.completeNow();
    }


    @Test
    public void pauseMonitoringLoop() {
        monitoringHandler.pauseMonitoringLoop();
        monitoringHandler.pauseMonitoringLoop();

        verify(spyVertx, times(2)).cancelTimer(-1L);
    }
}
