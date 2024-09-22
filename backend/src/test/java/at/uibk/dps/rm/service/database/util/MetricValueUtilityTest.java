package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link MetricValueUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueUtilityTest {

    private MetricValueUtility utility;

    @Mock
    private MetricValueRepository metricValueRepository;

    @Mock
    private PlatformMetricRepository platformMetricRepository;

    @Mock
    private Stage.Session session;

    private SessionManager sessionManager;

    private Resource r1;
    private SubResource sr1;
    private Platform pLambda;
    private MetricType mtNumber, mtString, mtBool;
    private Metric m1, m2, m3, m4;
    private PlatformMetric pmNumberMonitored, pmNumberManual, pmStringManual, pmBoolManual;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        utility = new MetricValueUtility(metricValueRepository, platformMetricRepository);
        pLambda = TestPlatformProvider.createPlatformFaas(1L, "lambda");
        mtNumber = TestMetricProvider.createMetricTypeNumber();
        mtString = TestMetricProvider.createMetricTypeString();
        mtBool = TestMetricProvider.createMetricTypeBoolean();
        m1 = TestMetricProvider.createMetric(1L, "m1", mtNumber);
        m2 = TestMetricProvider.createMetric(2L, "m2", mtNumber);
        m3 = TestMetricProvider.createMetric(4L, "m4", mtString);
        m4 = TestMetricProvider.createMetric(6L, "m6", mtBool);
        pmNumberMonitored = TestMetricProvider.createPlatformMetric(1L, m1, pLambda, true);
        pmNumberManual = TestMetricProvider.createPlatformMetric(2L, m2, pLambda, false);
        pmStringManual = TestMetricProvider.createPlatformMetric(4L, m3, pLambda, false);
        pmBoolManual = TestMetricProvider.createPlatformMetric(6L, m4, pLambda, false);
        r1 = TestResourceProvider.createResource(1L, pLambda);
        sr1 = TestResourceProvider.createSubResource(1L, "sr1", r1.getMain());
        sessionManager = new SessionManager(session);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkAddMetricListValid(boolean isMain, VertxTestContext testContext) {
        Resource resource = isMain ? r1 : sr1;
        JsonObject v1 = new JsonObject("{\"metric_id\": 2, \"value\": 2}");
        JsonObject v2 = new JsonObject("{\"metric_id\": 4, \"value\": \"value\"}");
        JsonObject v3 = new JsonObject("{\"metric_id\": 6, \"value\": true}");
        JsonArray values = new JsonArray(List.of(v1, v2, v3));

        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(resource.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m2.getMetricId())).thenReturn(Maybe.just(pmNumberManual));
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m3.getMetricId())).thenReturn(Maybe.just(pmStringManual));
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m4.getMetricId())).thenReturn(Maybe.just(pmBoolManual));
        when(session.persist(any(MetricValue.class))).thenReturn(CompletionStages.voidFuture());

        utility.checkAddMetricList(sessionManager, resource, values)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    verify(session, times(3)).persist(any(MetricValue.class));
                    testContext.completeNow();
                }),
                throwable -> testContext.failNow("method has thrown exception")
            );
    }

    @Test
    void checkAddMetricListInvalidMetricType(VertxTestContext testContext) {
        JsonObject v1 = new JsonObject("{\"metric_id\": 1, \"value\": \"value\"}");
        JsonArray values = new JsonArray(List.of(v1));

        sessionManager = new SessionManager(session);
        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(r1.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m1.getMetricId())).thenReturn(Maybe.just(pmNumberManual));

        utility.checkAddMetricList(sessionManager, r1, values)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("invalid value type for metric (2)");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricListSetMonitoredValue(VertxTestContext testContext) {
        JsonObject v1 = new JsonObject("{\"metric_id\": 1, \"value\": 2}");
        JsonArray values = new JsonArray(List.of(v1));

        sessionManager = new SessionManager(session);
        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(r1.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m1.getMetricId())).thenReturn(Maybe.just(pmNumberMonitored));

        utility.checkAddMetricList(sessionManager, r1, values)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("monitored metrics can't be set manually");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricListEmptyManualValue(VertxTestContext testContext) {
        JsonObject v1 = new JsonObject("{\"metric_id\": 1}");
        JsonArray values = new JsonArray(List.of(v1));

        sessionManager = new SessionManager(session);
        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(r1.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m1.getMetricId())).thenReturn(Maybe.just(pmNumberManual));

        utility.checkAddMetricList(sessionManager, r1, values)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("value can't be empty");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricPlatformMetricNotFound(VertxTestContext testContext) {
        JsonObject v1 = new JsonObject("{\"metric_id\": 1}");
        JsonArray values = new JsonArray(List.of(v1));

        sessionManager = new SessionManager(session);
        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(r1.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m1.getMetricId())).thenReturn(Maybe.empty());

        utility.checkAddMetricList(sessionManager, r1, values)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricMetricValueAlreadyExists(VertxTestContext testContext) {
        JsonObject v1 = new JsonObject("{\"metric_id\": 1}");
        JsonArray values = new JsonArray(List.of(v1));

        sessionManager = new SessionManager(session);
        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(r1.getResourceId()), anyLong()))
            .thenReturn(Maybe.just(new MetricValue()));
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m1.getMetricId())).thenReturn(Maybe.empty());

        utility.checkAddMetricList(sessionManager, r1, values)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }


    @Test
    void checkAddMetricMetricValueMetricNotSupportedForMainResource(VertxTestContext testContext) {
        JsonObject v2 = new JsonObject("{\"metric_id\": 2, \"value\": 2}");
        JsonArray values = new JsonArray(List.of(v2));
        pmNumberManual.setIsMainResourceMetric(false);

        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(r1.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m2.getMetricId())).thenReturn(Maybe.just(pmNumberManual));

        utility.checkAddMetricList(sessionManager, r1, values)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable).hasMessage("metric is not supported for main resources");
                    testContext.completeNow();
                })
            );
    }


    @Test
    void checkAddMetricMetricValueMetricNotSupportedForSubResource(VertxTestContext testContext) {
        JsonObject v2 = new JsonObject("{\"metric_id\": 2, \"value\": 2}");
        JsonArray values = new JsonArray(List.of(v2));
        pmNumberManual.setIsSubResourceMetric(false);

        when(metricValueRepository.findByResourceAndMetric(eq(sessionManager), eq(sr1.getResourceId()), anyLong()))
            .thenReturn(Maybe.empty());
        when(platformMetricRepository.findByPlatformAndMetric(sessionManager, pLambda.getPlatformId(),
            m2.getMetricId())).thenReturn(Maybe.just(pmNumberManual));

        utility.checkAddMetricList(sessionManager, sr1, values)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable).hasMessage("metric is not supported for sub resources");
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "number, test, false",
        "string, test, true",
        "bool, test, false",
        "string, null, false"
    })
    void metricTypeMatchesValueString(String type, String val, boolean expected) {
        MetricType metricType;
        switch (type){
            case "string":
                metricType = mtString;
                break;
            case "bool":
                metricType = mtBool;
                break;
            default:
                metricType = mtNumber;
        }
        String value = val;
        if (val.equals("null")) {
            value = null;
        }
        boolean result = MetricValueUtility.metricTypeMatchesValue(MetricTypeEnum.fromMetricType(metricType), value);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "number, 1.0, true",
        "string, 1.0, false",
        "bool, 1.0, false",
        "string, -1.0, false"
    })
    void metricTypeMatchesValueNumber(String type, double val, boolean expected) {
        MetricType metricType;
        switch (type){
            case "string":
                metricType = mtString;
                break;
            case "bool":
                metricType = mtBool;
                break;
            default:
                metricType = mtNumber;
        }
        Double value = val;
        if (val == -1.0) {
            value = null;
        }
        boolean result = MetricValueUtility.metricTypeMatchesValue(MetricTypeEnum.fromMetricType(metricType), value);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "number, true, false",
        "string, true, false",
        "bool, true, true",
        "string, false, false"
    })
    void metricTypeMatchesValueNumber(String type, boolean val, boolean expected) {
        MetricType metricType;
        switch (type){
            case "string":
                metricType = mtString;
                break;
            case "bool":
                metricType = mtBool;
                break;
            default:
                metricType = mtNumber;
        }
        Boolean value = val;
        if (!val) {
            value = null;
        }
        boolean result = MetricValueUtility.metricTypeMatchesValue(MetricTypeEnum.fromMetricType(metricType), value);
        assertThat(result).isEqualTo(expected);
    }


}
