package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
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
    private SessionFactory sessionFactory;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private Session session;

    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueService = new MetricValueServiceImpl(metricValueRepository, platformMetricRepository,
            smProvider);
    }

    @Test
    void testSaveAll(VertxTestContext testContext) {
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "availability", 0.99);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "latency", 40);
        List<JsonObject> metricValues = List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2));
        metricValues.forEach(entry -> entry.put("resource", new JsonObject("{\"resource_id\": 1}")));

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.createAll(eq(sessionManager), anyList())).thenReturn(Completable.complete());

        JsonArray data = new JsonArray(metricValues);

        metricValueService.saveAll(data, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneEntityExists(VertxTestContext testContext) {
        long entityId = 1L;
        MetricValue entity = new MetricValue();
        entity.setMetricValueId(entityId);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findByIdAndFetch(sessionManager, entityId)).thenReturn(Maybe.just(entity));

        metricValueService.findOne(entityId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource")).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void findOneEntityNotExists(VertxTestContext testContext) {
        long entityId = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findByIdAndFetch(sessionManager, entityId))
            .thenReturn(Maybe.empty());

        metricValueService.findOne(entityId, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithValue(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = true;
        Metric metric1 = new Metric();
        metric1.setMetricId(1L);
        Metric metric2 = new Metric();
        metric2.setMetricId(2L);
        MetricValue entity1 = new MetricValue();
        entity1.setMetricValueId(1L);
        entity1.setMetric(metric1);
        entity1.setValueNumber(10.0);
        MetricValue entity2 = new MetricValue();
        entity2.setMetricValueId(2L);
        entity2.setMetric(metric2);
        entity2.setValueString("ubuntu");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findAllByResourceAndFetch(sessionManager, resourceId))
            .thenReturn(Single.just(List.of(entity1, entity2)));

        metricValueService.findAllByResource(resourceId, includeValue, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(0).getDouble("value_number")).isEqualTo(10.0);
            assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
            assertThat(result.getJsonObject(1).getString("value_string")).isEqualTo("ubuntu");
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithoutValue(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = false;
        Metric metric1 = new Metric();
        metric1.setMetricId(1L);
        Metric metric2 = new Metric();
        metric2.setMetricId(2L);
        MetricValue entity1 = new MetricValue();
        entity1.setMetric(metric1);
        MetricValue entity2 = new MetricValue();
        entity2.setMetric(metric2);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findAllByResourceAndFetch(sessionManager, resourceId))
            .thenReturn(Single.just(List.of(entity1, entity2)));

        metricValueService.findAllByResource(resourceId, includeValue,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void updateByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        double valueNumber = 13.37;
        Metric metric = TestMetricProvider.createMetric(metricId, "metric",
            TestMetricProvider.createMetricTypeNumber());
        MetricValue metricValue = TestMetricProvider
            .createMetricValue(1L, metric, valueNumber);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, "platform");
        PlatformMetric platformMetric = TestMetricProvider.createPlatformMetric(2L, metric, platform,
            false);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findByResourceAndMetricAndFetch(sessionManager, resourceId, metricId))
            .thenReturn(Maybe.just(metricValue));
        when(platformMetricRepository.findByResourceAndMetric(sessionManager, resourceId, metricId))
            .thenReturn(Maybe.just(platformMetric));


        metricValueService.updateByResourceAndMetric(resourceId, metricId, null, valueNumber,
            null, true, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result).isNull();
            testContext.completeNow();
        })));
    }

    @Test
    void deleteByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        MetricValue mv1 = new MetricValue();

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findByResourceAndMetric(sessionManager, resourceId, metricId))
            .thenReturn(Maybe.just(mv1));
        when(session.remove(mv1)).thenReturn(CompletionStages.voidFuture());

        metricValueService.deleteByResourceAndMetric(resourceId, metricId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void deleteByResourceAndMetricNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findByResourceAndMetric(sessionManager, resourceId, metricId))
            .thenReturn(Maybe.empty());

        metricValueService.deleteByResourceAndMetric(resourceId, metricId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("MetricValue not found");
                testContext.completeNow();
        })));
    }
}
