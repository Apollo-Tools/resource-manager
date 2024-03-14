package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.monitoring.RegionConnectivity;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricpusher.RegionMetricPushService;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestConnectivityProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.monitoring.LatencyMonitoringUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link RegionMonitoringHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegionMonitoringHandlerTest {
    private RegionMonitoringHandler monitoringHandler;

    private Vertx spyVertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private RegionService regionService;

    @Mock
    private RegionMetricPushService regionMetricPushService;

    @Mock
    private LatencyMonitoringUtility latencyMonitoringUtility;

    @Mock
    private ProcessOutput po1, po2, po3;

    @Mock
    private Process p1, p2, p3;

    private static Logger logger;

    private static MockedStatic<LoggerFactory> mockedLoggerFactory;

    private Region reg1, reg2, reg3;

    @BeforeAll
    static void initAll() {
        logger = mock(Logger.class);
        mockedLoggerFactory = mockStatic(LoggerFactory.class);
        mockedLoggerFactory.when(() -> LoggerFactory.getLogger(RegionMonitoringHandler.class)).thenReturn(logger);
    }

    @AfterAll
    static void cleanupAll() {
        mockedLoggerFactory.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        clearInvocations(logger);
        JsonMapperConfig.configJsonMapper();
        ConfigDTO configDTO = TestConfigProvider.getConfigDTO();
        spyVertx = spy(vertx);
        monitoringHandler = new RegionMonitoringHandler(spyVertx, configDTO, serviceProxyProvider,
            latencyMonitoringUtility);
        reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        reg2 = TestResourceProviderProvider.createRegion(2L, "us-east-2");
        reg3 = TestResourceProviderProvider.createRegion(3L, "us-west-1");

        lenient().when(serviceProxyProvider.getRegionService()).thenReturn(regionService);
        lenient().when(serviceProxyProvider.getRegionMetricPushService()).thenReturn(regionMetricPushService);
        lenient().when(spyVertx.fileSystem()).thenReturn(fileSystem);
    }

    @Test
    public void startMonitoringLoopNoRegions(VertxTestContext testContext) throws InterruptedException {
        when(regionService.findAllByProviderName(ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.just(new JsonArray()));
        when(regionMetricPushService.composeAndPushMetrics(new JsonArray())).thenReturn(Completable.complete());

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
        verify(logger, times(2)).info(loggerInfo.capture());
        assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Started: monitor regions");
        assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("Finished: monitor regions");
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoop(VertxTestContext testContext) throws InterruptedException {
        JsonArray regions = new JsonArray(Json.encode(List.of(reg1, reg2, reg3)));
        RegionConnectivity rc1 = TestConnectivityProvider.createRegionConnectivity(reg1, true,0.123);
        RegionConnectivity rc2 = TestConnectivityProvider.createRegionConnectivity(reg2, false, null);
        RegionConnectivity rc3 = TestConnectivityProvider.createRegionConnectivity(reg3, true, 0.321);
        JsonArray connectivites = new JsonArray(Json.encode(List.of(rc1, rc2, rc3)));
        when(regionService.findAllByProviderName(ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.just(regions));
        when(latencyMonitoringUtility.getPingUrl(reg1)).thenReturn("us-east-1");
        when(latencyMonitoringUtility.getPingUrl(reg2)).thenReturn("us-east-2");
        when(latencyMonitoringUtility.getPingUrl(reg3)).thenReturn("us-west-1");
        when(latencyMonitoringUtility.measureLatency(2, "us-east-1")).thenReturn(Single.just(po1));
        when(po1.getProcess()).thenReturn(p1);
        when(p1.exitValue()).thenReturn(0);
        when(po1.getOutput()).thenReturn("123");
        when(latencyMonitoringUtility.measureLatency(2, "us-east-2")).thenReturn(Single.just(po2));
        when(po2.getProcess()).thenReturn(p2);
        when(p2.exitValue()).thenReturn(1);
        when(po2.getOutput()).thenReturn("offline");
        when(latencyMonitoringUtility.measureLatency(2, "us-west-1")).thenReturn(Single.just(po3));
        when(po3.getProcess()).thenReturn(p3);
        when(p3.exitValue()).thenReturn(0);
        when(po3.getOutput()).thenReturn("321");
        when(regionMetricPushService.composeAndPushMetrics(connectivites)).thenReturn(Completable.complete());

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
        verify(logger, times(6)).info(loggerInfo.capture());
        assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Started: monitor regions");
        assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("Monitor latency: us-east-1");
        assertThat(loggerInfo.getAllValues().get(2)).isEqualTo("Monitor latency: us-east-2");
        assertThat(loggerInfo.getAllValues().get(3)).isEqualTo("Region us-east-2 not reachable: offline");
        assertThat(loggerInfo.getAllValues().get(4)).isEqualTo("Monitor latency: us-west-1");
        assertThat(loggerInfo.getAllValues().get(5)).isEqualTo("Finished: monitor regions");
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopError(VertxTestContext testContext) throws InterruptedException {
        when(regionService.findAllByProviderName(ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.error(new BadInputException("error")));

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(300000L), any());
        ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
        verify(logger).info(loggerInfo.capture());
        assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Started: monitor regions");
        ArgumentCaptor<String> loggerError = ArgumentCaptor.forClass(String.class);
        verify(logger).error(loggerError.capture());
        assertThat(loggerError.getAllValues().get(0)).isEqualTo("error");
        testContext.completeNow();
    }


    @Test
    public void pauseMonitoringLoop() {
        monitoringHandler.pauseMonitoringLoop();
        monitoringHandler.pauseMonitoringLoop();

        verify(spyVertx, times(2)).cancelTimer(-1L);
    }
}
