package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link MetricValueRepository} class.
 *
 * @author matthi-g
 */
public class MetricValueRepositoryTest extends DatabaseTest {

    private final MetricValueRepository repository = new MetricValueRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-2");
            Platform p1 = TestPlatformProvider.createPlatformContainer(1L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg2);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            Metric mDeploymentRole = TestMetricProvider.createMetric(20L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mDeploymentRole, r1,
                "labrole");
            MetricValue mv3 = TestMetricProvider.createMetricValue(null, mAvailability, r2, 0.99);
            MetricValue mv4 = TestMetricProvider.createMetricValue(null, mDeploymentRole, r2,
                "labrole");
            return sessionManager.persist(r1)
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(mv1))
                .flatMap(res -> sessionManager.persist(mv2))
                .flatMap(res -> sessionManager.persist(mv3))
                .flatMap(res -> sessionManager.persist(mv4));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, true, availability",
        "2, true, deployment-role",
        "3, true, availability",
        "4, true, deployment-role",
        "5, false, non-existent"
    })
    void findByIdAndFetch(long id, boolean exists, String metric, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, id))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getMetricValueId()).isEqualTo(id);
                    assertThat(result.getMetric().getMetric()).isEqualTo(metric);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 2",
        "2, 3, 4",
        "3, -1, -1",
    })
    void findAllByDeploymentIdAndAccountId(long resourceId, long id1, long id2, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByResourceAndFetch(sessionManager, resourceId))
            .subscribe(result -> testContext.verify(() -> {
                if (id1 == -1) {
                    assertThat(result.size()).isEqualTo(0);
                } else {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.get(0).getMetricValueId()).isEqualTo(id1);
                    assertThat(result.get(0).getMetric().getMetric()).isEqualTo("availability");
                    assertThat(result.get(1).getMetricValueId()).isEqualTo(id2);
                    assertThat(result.get(1).getMetric().getMetric()).isEqualTo("deployment-role");
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true, 1",
        "1, 20, true, 2",
        "1, 2, false, -1",
        "2, 1, true, 3",
        "2, 20, true, 4",
        "1, 2, false, -1",
        "3, 1, false, -1",
    })
    void findByResourceAndMetric(long resourceId, long metricId, boolean exists, long metricValueId,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByResourceAndMetric(sessionManager,
                resourceId, metricId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getMetricValueId()).isEqualTo(metricValueId);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true, 1, availability, number",
        "1, 20, true, 2, deployment-role, string",
        "1, 2, false, -1, none, none",
        "2, 1, true, 3, availability, number",
        "2, 20, true, 4, deployment-role, string",
        "1, 2, false, -1, none, none",
        "3, 1, false, -1, none, none",
    })
    void findByResourceAndMetricAndFetch(long resourceId, long metricId, boolean exists, long metricValueId,
            String metric, String metricType, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByResourceAndMetricAndFetch(sessionManager,
                resourceId, metricId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getMetricValueId()).isEqualTo(metricValueId);
                    assertThat(result.getMetric().getMetric()).isEqualTo(metric);
                    assertThat(result.getMetric().getMetricType().getType()).isEqualTo(metricType);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }
}
