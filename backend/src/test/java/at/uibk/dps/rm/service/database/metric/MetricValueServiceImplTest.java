package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.util.MetricValueUtility;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.DatabaseUtilMockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link MetricValueServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueServiceImplTest {

    private MetricValueService metricValueService;

    @Mock
    private MetricValueRepository metricValueRepository;

    @Mock
    private PlatformMetricRepository platformMetricRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private Resource r1;
    private Metric mString, mBool;
    private MetricValue mvString, mvNumber, mvBool;
    PlatformMetric pmString, pmNumber, pmBool;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueService = new MetricValueServiceImpl(metricValueRepository, platformMetricRepository,
            smProvider);
        r1 = TestResourceProvider.createResource(1L);
        MetricType mtString = TestMetricProvider.createMetricTypeString();
        MetricType mtNumber = TestMetricProvider.createMetricTypeNumber();
        MetricType mtBool = TestMetricProvider.createMetricTypeBoolean();
        mString = TestMetricProvider.createMetric(1L, "os", mtString);
        Metric mNumber = TestMetricProvider.createMetric(2L, "availability", mtNumber);
        mBool = TestMetricProvider.createMetric(3L, "online", mtBool);
        Platform p1 = TestPlatformProvider.createPlatformFaas(1L, "platform");
        pmString = TestMetricProvider.createPlatformMetric(2L, mString, p1, false);
        pmNumber = TestMetricProvider.createPlatformMetric(2L, mNumber, p1, false);
        pmBool = TestMetricProvider.createPlatformMetric(2L, mBool, p1, true);
        mvString = TestMetricProvider.createMetricValue(1L, mString, "ubuntu");
        mvNumber = TestMetricProvider.createMetricValue(2L, mNumber, 0.99);
        mvBool = TestMetricProvider.createMetricValue(3L, mBool, true);
    }

    @Test
    void testSaveAll(VertxTestContext testContext) {
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mvString), JsonObject.mapFrom(mvNumber)));

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(sessionManager.find(Resource.class, r1.getResourceId())).thenReturn(Maybe.just(r1));
        try (MockedConstruction<MetricValueUtility> ignored = DatabaseUtilMockprovider
                .mockMetricValueUtilitySave(sessionManager, r1, metricValues)) {
            metricValueService.saveAllToResource(r1.getResourceId(), metricValues,
                testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
        }
    }

    @Test
    void testSaveAllResourceNotFound(VertxTestContext testContext) {
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mvString), JsonObject.mapFrom(mvNumber)));

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(sessionManager.find(Resource.class, r1.getResourceId())).thenReturn(Maybe.empty());
        metricValueService.saveAllToResource(r1.getResourceId(), metricValues,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneEntityExists(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(metricValueRepository.findByIdAndFetch(sessionManager, mvString.getMetricValueId()))
            .thenReturn(Maybe.just(mvString));

        metricValueService.findOne(mvString.getMetricValueId(), testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.getLong("metric_value_id")).isEqualTo(1L);
            assertThat(result.getJsonObject("resource")).isNull();
            testContext.completeNow();
        })));
    }

    @Test
    void findOneEntityNotExists(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(metricValueRepository.findByIdAndFetch(sessionManager, mvString.getMetricValueId()))
            .thenReturn(Maybe.empty());

        metricValueService.findOne(mvString.getMetricValueId(), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithValue(VertxTestContext testContext) {
        boolean includeValue = true;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(metricValueRepository.findAllByResourceAndFetch(sessionManager, r1.getResourceId()))
            .thenReturn(Single.just(List.of(mvString, mvNumber)));

        metricValueService.findAllByResource(r1.getResourceId(), includeValue,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getString("value_string")).isEqualTo("ubuntu");
                assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getDouble("value_number")).isEqualTo(0.99);
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithoutValue(VertxTestContext testContext) {
        boolean includeValue = false;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(metricValueRepository.findAllByResourceAndFetch(sessionManager, r1.getResourceId()))
            .thenReturn(Single.just(List.of(mvString, mvNumber)));

        metricValueService.findAllByResource(r1.getResourceId(), includeValue,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(strings = {"string", "number", "bool"})
    void updateByResourceAndMetric(String type, VertxTestContext testContext) {
        Metric metric;
        MetricValue metricValue;
        PlatformMetric platformMetric;
        String valueString = null;
        Double valueNumber = null;
        Boolean valueBool = null;
        switch (type) {
            case "string":
                metricValue = mvString;
                platformMetric = pmString;
                valueString = "TempleOS";
                break;
            case "number":
                metricValue = mvNumber;
                platformMetric = pmNumber;
                valueNumber = 13.37;
                break;
            case "bool":
            default:
                metricValue = mvBool;
                platformMetric = pmBool;
                valueBool = false;
                break;
        }
        metric = metricValue.getMetric();

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricValueRepository.findByResourceAndMetricAndFetch(sessionManager, r1.getResourceId(),
            metric.getMetricId())).thenReturn(Maybe.just(metricValue));
        when(platformMetricRepository.findByResourceAndMetric(sessionManager, r1.getResourceId(), metric.getMetricId()))
            .thenReturn(Maybe.just(platformMetric));

        metricValueService.updateByResourceAndMetric(r1.getResourceId(), metric.getMetricId(), valueString,
            valueNumber, valueBool, testContext.succeeding(result -> testContext.verify(() -> {
                switch (type) {
                    case "string":
                        assertThat(metricValue.getValueString()).isEqualTo("TempleOS");
                        break;
                    case "number":
                        assertThat(metricValue.getValueNumber()).isEqualTo(BigDecimal.valueOf(13.37));
                        break;
                    case "bool":
                    default:
                        assertThat(metricValue.getValueBool()).isEqualTo(false);
                        break;
                }
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(strings = {"string", "number", "bool"})
    void updateByResourceAndMetricInvalidMetricType(String type, VertxTestContext testContext) {
        Metric metric;
        MetricValue metricValue;
        PlatformMetric platformMetric;
        String valueString = null;
        Double valueNumber = null;
        Boolean valueBool = null;
        boolean isExternalSource = true;
        switch (type) {
            case "string":
                metricValue = mvString;
                platformMetric = pmString;
                valueNumber = 10.5;
                break;
            case "number":
                metricValue = mvNumber;
                platformMetric = pmNumber;
                valueBool = true;
                break;
            case "bool":
            default:
                metricValue = mvBool;
                platformMetric = pmBool;
                valueString = "false";
                isExternalSource = false;
                break;
        }
        metric = metricValue.getMetric();

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricValueRepository.findByResourceAndMetricAndFetch(sessionManager, r1.getResourceId(),
            metric.getMetricId())).thenReturn(Maybe.just(metricValue));
        when(platformMetricRepository.findByResourceAndMetric(sessionManager, r1.getResourceId(), metric.getMetricId()))
            .thenReturn(Maybe.just(platformMetric));

        metricValueService.updateByResourceAndMetric(r1.getResourceId(), metric.getMetricId(), valueString,
            valueNumber, valueBool, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("invalid metric type");
                testContext.completeNow();
            })));
    }

    @Test
    void updateByResourceAndMetricMonitoredMetric(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricValueRepository.findByResourceAndMetricAndFetch(sessionManager, r1.getResourceId(),
            mBool.getMetricId())).thenReturn(Maybe.just(mvBool));
        when(platformMetricRepository.findByResourceAndMetric(sessionManager, r1.getResourceId(),
            mBool.getMetricId())).thenReturn(Maybe.just(pmBool));

        metricValueService.updateByResourceAndMetric(r1.getResourceId(), mBool.getMetricId(), null,
            null, true, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("monitored metrics can't be updated manually");
                testContext.completeNow();
            })));
    }

    @Test
    void deleteByResourceAndMetric(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricValueRepository.findByResourceAndMetric(sessionManager, r1.getResourceId(),
            mvString.getMetric().getMetricId())).thenReturn(Maybe.just(mvString));
        when(sessionManager.remove(mvString)).thenReturn(Completable.complete());

        metricValueService.deleteByResourceAndMetric(r1.getResourceId(), mString.getMetricId(),
            testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void deleteByResourceAndMetricNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricValueRepository.findByResourceAndMetric(sessionManager, r1.getResourceId(),
            mvString.getMetric().getMetricId())).thenReturn(Maybe.empty());

        metricValueService.deleteByResourceAndMetric(r1.getResourceId(), mString.getMetricId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
        })));
    }
}
