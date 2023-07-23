package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
import java.util.concurrent.CompletionStage;

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
    MetricValueRepository metricValueRepository;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueService = new MetricValueServiceImpl(metricValueRepository, sessionFactory);
    }

    @Test
    void testSaveAll(VertxTestContext testContext) {
        CompletionStage<Void> completionStage = CompletionStages.voidFuture();
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "availability", 0.99);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "latency", 40);
        List<JsonObject> metricValues = List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2));
        metricValues.forEach(entry -> entry.put("resource", new JsonObject("{\"resource_id\": 1}")));

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.createAll(eq(session), anyList())).thenReturn(completionStage);

        JsonArray data = new JsonArray(metricValues);

        metricValueService.saveAll(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneEntityExists(VertxTestContext testContext) {
        long entityId = 1L;
        MetricValue entity = new MetricValue();
        entity.setMetricValueId(entityId);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(metricValueRepository.findByIdAndFetch(session, entityId))
            .thenReturn(CompletionStages.completedFuture(entity));

        metricValueService.findOne(entityId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource")).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void findOneEntityNotExists(VertxTestContext testContext) {
        long entityId = 1L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(metricValueRepository.findByIdAndFetch(session, entityId))
            .thenReturn(CompletionStages.completedFuture(null));

        metricValueService.findOne(entityId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
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

        SessionMockHelper.mockSession(sessionFactory, session);
        when(metricValueRepository.findByResourceAndFetch(session, resourceId))
            .thenReturn(CompletionStages.completedFuture(List.of(entity1, entity2)));

        metricValueService.findAllByResource(resourceId, includeValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
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

        SessionMockHelper.mockSession(sessionFactory, session);
        when(metricValueRepository.findByResourceAndFetch(session, resourceId))
            .thenReturn(CompletionStages.completedFuture(List.of(entity1, entity2)));

        metricValueService.findAllByResource(resourceId, includeValue)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                    testContext.completeNow();
                })));
    }

    @Test
    void checkOneByResourceAndMetricExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        MetricValue entity = new MetricValue();

        SessionMockHelper.mockSession(sessionFactory, session);
        when(metricValueRepository.findByResourceAndMetric(session, resourceId, metricId))
            .thenReturn(CompletionStages.completedFuture(entity));

        metricValueService.existsOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
        })));
    }

    @Test
    void checkOneByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(metricValueRepository.findByResourceAndMetric(session, resourceId, metricId))
            .thenReturn(CompletionStages.completedFuture(null));

        metricValueService.existsOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
        })));
    }

    @Test
    void updateByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        double valueNumber = 13.37;
        Metric metric = TestMetricProvider.createMetric(metricId, "metric",
            TestMetricProvider.createMetricTypeNumber(), false);
        MetricValue metricValue = TestMetricProvider
            .createMetricValue(1L, metric, valueNumber);

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.findByResourceAndMetricAndFetch(session, resourceId, metricId))
            .thenReturn(CompletionStages.completedFuture(metricValue));


        metricValueService.updateByResourceAndMetric(resourceId, metricId, null, valueNumber,
                null, true)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void deleteByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(metricValueRepository.deleteByResourceAndMetric(session, resourceId, metricId))
            .thenReturn(CompletionStages.completedFuture(0));

        metricValueService.deleteByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }
}
