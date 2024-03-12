package at.uibk.dps.rm.handler.alerting;

import at.uibk.dps.rm.entity.alerting.AlertMessage;
import at.uibk.dps.rm.entity.alerting.AlertType;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentAlertingDTO;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.SerializationException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.testutil.mockprovider.SLOMockProvider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link AlertingHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AlertingHandlerTest {

    private AlertingHandler alertingHandler;

    private Vertx spyVertx;

    @Mock
    private WebClient webClient;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private MetricQueryService metricQueryService;

    private static Logger logger;

    @Mock
    private HttpRequest<Buffer> httpRequest;

    @Mock
    private HttpResponse<Buffer> httpResponse;

    private static MockedStatic<LoggerFactory> mockedLoggerFactory;

    private DeploymentAlertingDTO deploymentAlertingDTO;
    private Resource r1;

    @BeforeAll
    static void initAll() {
        logger = mock(Logger.class);
        mockedLoggerFactory = mockStatic(LoggerFactory.class);
        mockedLoggerFactory.when(() -> LoggerFactory.getLogger(AlertingHandler.class)).thenReturn(logger);
    }

    @AfterAll
    static void cleanupAll() {
        mockedLoggerFactory.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        ConfigDTO configDTO = TestConfigProvider.getConfigDTO();
        configDTO.setRegionMonitoringPeriod(5.0);
        r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        EnsembleSLO eSLO1 = TestEnsembleProvider.createEnsembleSLOGT(1L, "latency", 1L, 0.2);
        EnsembleSLO eSLO2 = TestEnsembleProvider.createEnsembleSLOGT(2L, "cpu%", 1L, 70);
        EnsembleSLO eSLO3 = TestEnsembleProvider.createEnsembleSLOGT(3L, "memory%", 1L, 70);
        deploymentAlertingDTO = TestDTOProvider.createDeploymentAlertingDTO(1L, 2L,
            List.of(r1, r2), List.of(eSLO1, eSLO2, eSLO3));

        spyVertx = spy(vertx);
        alertingHandler = new AlertingHandler(spyVertx, webClient, configDTO, deploymentService,
            metricQueryService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testStartValidationLoopNoDeployments(boolean pauseLoop, VertxTestContext testContext)
            throws InterruptedException {
        clearInvocations(logger);
        when(deploymentService.findAllActiveWithAlerting()).thenReturn(Single.just(new JsonArray()));

        alertingHandler.startValidationLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        if (pauseLoop) {
            alertingHandler.pauseValidationLoop();
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx, times(1)).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger).info(loggerInfo.capture());
            assertThat(loggerInfo.getValue()).isEqualTo("Finished: validation of deployments");
        } else {
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx, times(2)).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(2)).info(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Finished: validation of deployments");
            assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("Finished: validation of deployments");
        }
        testContext.completeNow();
    }

    @Test
    public void testStartValidationLoopNoBreach(VertxTestContext testContext) throws InterruptedException {
        clearInvocations(logger);
        JsonObject alertingDTO = JsonObject.mapFrom(deploymentAlertingDTO);
        when(deploymentService.findAllActiveWithAlerting())
            .thenReturn(Single.just(new JsonArray(List.of(alertingDTO))));

        try (MockedConstruction<SLOValidator> ignoreSLOValidator =
                 SLOMockProvider.mockSLOValidatorValidate(deploymentAlertingDTO, List.of())) {
            alertingHandler.startValidationLoop();
            testContext.awaitCompletion(1, TimeUnit.SECONDS);
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(2)).info(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Validate resources of deployment: 1");
            assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("Finished: validation of deployments");
            testContext.completeNow();
        }
    }

    @Test
    public void testStartValidationLoopBreachSuccessfulNotification() {
        clearInvocations(logger);
        MonitoredMetricValue monitoredMetricValue =
            TestMetricProvider.createMonitoredMetricValue(MonitoringMetricEnum.CPU_UTIL, 95.0);
        r1.getMonitoredMetricValues().add(monitoredMetricValue);
        JsonObject alertingDTO = JsonObject.mapFrom(deploymentAlertingDTO);
        AlertMessage am = new AlertMessage(AlertType.SLO_BREACH, r1.getResourceId(), monitoredMetricValue);
        when(deploymentService.findAllActiveWithAlerting())
            .thenReturn(Single.just(new JsonArray(List.of(alertingDTO))));
        when(webClient.postAbs("http://localhost:9999")).thenReturn(httpRequest);
        when(httpRequest.timeout(5000)).thenReturn(httpRequest);
        when(httpRequest.sendJsonObject(argThat((JsonObject requestBody) -> {
            AlertMessage alert = requestBody.mapTo(AlertMessage.class);
            return alert.getType().equals(am.getType()) && alert.getResourceId() == am.getResourceId() &&
                alert.getMetric().equals(am.getMetric()) && alert.getValue().equals(am.getValue());
        }))).thenReturn(Single.just(httpResponse));
        when(httpResponse.statusCode()).thenReturn(204);

        try (MockedConstruction<SLOValidator> ignoreSLOValidator =
                 SLOMockProvider.mockSLOValidatorValidate(deploymentAlertingDTO, List.of(r1))) {
            alertingHandler.startValidationLoop();
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(3)).info(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Validate resources of deployment: 1");
            assertThat(loggerInfo.getAllValues().get(1)).startsWith("{\"type\":\"SLO_BREACH\",\"resource_id\":1," +
                "\"metric\":\"cpu%\",\"value\":95.0,\"timestamp\":");
            assertThat(loggerInfo.getAllValues().get(2)).isEqualTo("Finished: validation of deployments");
        }
    }

    @Test
    public void testStartValidationLoopBreachFailedNotification() {
        clearInvocations(logger);
        MonitoredMetricValue monitoredMetricValue =
            TestMetricProvider.createMonitoredMetricValue(MonitoringMetricEnum.CPU_UTIL, 95.0);
        r1.getMonitoredMetricValues().add(monitoredMetricValue);
        JsonObject alertingDTO = JsonObject.mapFrom(deploymentAlertingDTO);
        AlertMessage am = new AlertMessage(AlertType.SLO_BREACH, r1.getResourceId(), monitoredMetricValue);
        when(deploymentService.findAllActiveWithAlerting())
            .thenReturn(Single.just(new JsonArray(List.of(alertingDTO))));
        when(webClient.postAbs("http://localhost:9999")).thenReturn(httpRequest);
        when(httpRequest.timeout(5000)).thenReturn(httpRequest);
        when(httpRequest.sendJsonObject(argThat((JsonObject requestBody) -> {
            AlertMessage alert = requestBody.mapTo(AlertMessage.class);
            return alert.getType().equals(am.getType()) && alert.getResourceId() == am.getResourceId() &&
                alert.getMetric().equals(am.getMetric()) && alert.getValue().equals(am.getValue());
        }))).thenReturn(Single.error(BadInputException::new));

        try (MockedConstruction<SLOValidator> ignoreSLOValidator =
                 SLOMockProvider.mockSLOValidatorValidate(deploymentAlertingDTO, List.of(r1))) {
            alertingHandler.startValidationLoop();
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(4)).info(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Validate resources of deployment: 1");
            assertThat(loggerInfo.getAllValues().get(1)).startsWith("{\"type\":\"SLO_BREACH\",\"resource_id\":1," +
                "\"metric\":\"cpu%\",\"value\":95.0,\"timestamp\":");
            assertThat(loggerInfo.getAllValues().get(2)).startsWith("Failed to notify client status code: 421");
            assertThat(loggerInfo.getAllValues().get(3)).isEqualTo("Finished: validation of deployments");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testStartValidationLoopError(boolean pauseLoop, VertxTestContext testContext) throws InterruptedException {
        clearInvocations(logger);
        when(deploymentService.findAllActiveWithAlerting()).thenReturn(Single.error(SerializationException::new));

        alertingHandler.startValidationLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        if (pauseLoop) {
            alertingHandler.pauseValidationLoop();
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx, times(1)).setTimer(anyLong(), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(0)).info(any());
            verify(logger, times(1)).error(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("the requested operation could not be " +
                "completed due to a serialization conflict. Please retry the operation.");
        } else {
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx, times(2)).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(0)).info(any());
            verify(logger, times(2)).error(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("the requested operation could not be " +
                "completed due to a serialization conflict. Please retry the operation.");
            assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("the requested operation could not be " +
                "completed due to a serialization conflict. Please retry the operation.");
        }
        testContext.completeNow();
    }
}
