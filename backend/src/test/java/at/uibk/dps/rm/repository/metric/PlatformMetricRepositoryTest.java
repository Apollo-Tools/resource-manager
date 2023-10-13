package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformMetricRepositoryTest extends DatabaseTest {

    private final PlatformMetricRepository repository = new PlatformMetricRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionCompletable(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-2");
            Platform p1 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg2);
            SubResource sr1 = TestResourceProvider.createSubResourceWithoutMVs(null, "r3", (MainResource) r1);
            SubResource sr2 = TestResourceProvider.createSubResourceWithoutMVs(null, "r4", (MainResource) r2);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            Metric mClusterUrl = TestMetricProvider.createMetric(12L);
            Metric mPrePullTimeout = TestMetricProvider.createMetric(17L);
            Metric mHostname = TestMetricProvider.createMetric(21L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mClusterUrl, r1, "localhost");
            MetricValue mv3 = TestMetricProvider.createMetricValue(null, mPrePullTimeout, r1, 2.0);
            MetricValue mv5 = TestMetricProvider.createMetricValue(null, mAvailability, r2, 0.99);
            MetricValue mv6 = TestMetricProvider.createMetricValue(null, mHostname, sr1, "node1");
            return sessionManager.persist(r1)
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(sr1))
                .flatMap(res -> sessionManager.persist(sr2))
                .flatMapCompletable(res -> sessionManager.persist(new MetricValue[]{mv1, mv2, mv3, mv5, mv6}));
        }).blockingSubscribe(() -> {}, testContext::failNow);
    }

    private static Stream<Arguments> providePlatformMetrics() {
        return Stream.of(
            Arguments.of(1L, List.of(38L, 1L, 3L, 5L, 2L, 7L), List.of("deployment-role", "availability",
                "latency", "online", "cost", "time-to-live"), List.of("string", "number", "number", "boolean", "number",
                "number")),
            Arguments.of(2L, List.of(10L, 12L, 34L, 8L, 11L, 13L, 9L, 15L, 46L), List.of("cpu",
                "memory-size", "instance-type", "availability", "latency", "online", "cost", "time-to-live",
                "docker-architecture"), List.of("number", "number", "string", "number", "number", "boolean", "number",
                "number", "string")),
            Arguments.of(3L, List.of(35L, 36L, 37L, 18L, 20L, 16L, 19L, 21L, 17L, 23L, 44L, 45L,
                47L), List.of("gateway-url", "openfaas-user", "openfaas-pw", "cpu", "memory-size",
                "availability", "latency", "online", "cost", "time-to-live", "cpu-available", "memory-size-available",
                "docker-architecture"), List.of("string", "string", "string", "number", "number", "number",
                "number", "boolean", "number", "number", "number", "number", "string")),
            Arguments.of(4L, List.of(25L, 28L, 27L, 30L, 32L, 24L, 29L, 31L, 26L, 33L, 39L, 40L,
                    41L, 42L, 43L), List.of("cluster-url", "external-ip", "cpu", "memory-size",
                    "pre-pull-timeout", "availability", "latency", "online", "cost", "time-to-live", "hostname",
                    "storage-size", "storage-size-available", "cpu-available", "memory-size-available"),
                List.of("string", "string", "number", "number", "number", "number", "number", "boolean",
                    "number", "number", "string", "number", "number", "number", "number")),
            Arguments.of(99L, List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("providePlatformMetrics")
    void findAllByPlatformId(long platformId, List<Long> platformMetricIds, List<String> metrics,
                             List<String> metricTypes, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByPlatform(sessionManager, platformId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(PlatformMetric::getPlatformMetricId).collect(Collectors.toList()))
                    .isEqualTo(platformMetricIds);
                assertThat(result.stream().map(pm -> pm.getMetric().getMetric()).collect(Collectors.toList()))
                    .isEqualTo(metrics);
                assertThat(result.stream().map(pm -> pm.getMetric().getMetricType().getType())
                    .collect(Collectors.toList()))
                    .isEqualTo(metricTypes);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true, 1, availability, number",
        "2, 1, true, 8, availability, number",
        "3, 1, true, 16, availability, number",
        "4, 1, true, 24, availability, number",
        "5, 1, false, -1, none, none",
        "1, 22, false, -1, none, none",
        "2, 22, false, -1, none, none",
        "3, 22, false, -1, none, none",
        "4, 4, false, 1, none, none"
    })
    void findByPlatformAndMetric(long platformId, long metricId, boolean exists, long metricValueId, String metric,
                                 String type, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByPlatformAndMetric(sessionManager, platformId, metricId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getPlatformMetricId()).isEqualTo(metricValueId);
                    assertThat(result.getMetric().getMetric()).isEqualTo(metric);
                    assertThat(result.getMetric().getMetricType().getType()).isEqualTo(type);
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
        "1, 1, true, 24",
        "1, 21, false, -1",
        "2, 19, true, 33",
        "2, 4, false, -8",
        "3, 1, true, 24",
        "3, 4, false, -1",
        "4, 21, true, 39",
        "4, 19, false, -1"
    })
    void findByResourceAndMetric(long resourceId, long metricId, boolean exists, long metricValueId,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByResourceAndMetric(sessionManager, resourceId, metricId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getPlatformMetricId()).isEqualTo(metricValueId);
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
        "1, true, 0",
        "1, false, 0",
        "2, true, 2",
        "2, false, 0",
        "3, true, 0",
        "3, false, 0",
        "4, true, 0",
        "4, false, 3",
    })
    void countMissingRequiredMetricValuesByResourceId(long resourceId, boolean isMainResource, long count,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .countMissingRequiredMetricValuesByResourceId(sessionManager, resourceId, isMainResource))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(count);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
