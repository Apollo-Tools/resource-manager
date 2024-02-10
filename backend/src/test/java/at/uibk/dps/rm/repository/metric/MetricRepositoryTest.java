package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link MetricRepository} class.
 *
 * @author matthi-g
 */
public class MetricRepositoryTest extends DatabaseTest {

    private final MetricRepository repository = new MetricRepository();

    private static Stream<Arguments> providePlatformMetrics() {
        return Stream.of(
            Arguments.of(1L, true, List.of("deployment-role")),
            Arguments.of(1L, false, List.of("availability", "latency", "online", "cost", "time-to-live")),
            Arguments.of(2L, true, List.of("memory-size", "instance-type", "docker-architecture")),
            Arguments.of(2L, false, List.of("availability", "latency", "online", "cpu", "cost",
                "time-to-live")),
            Arguments.of(99L, true, List.of()),
            Arguments.of(99L, false, List.of())
        );
    }

/*
    @ParameterizedTest
    @MethodSource("providePlatformMetrics")
    void findAllByPlatformId(long platformId, boolean required, List<String> metrics,
            VertxTestContext testContext) {
        // remove
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByPlatformId(sessionManager,
                platformId, required))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Metric::getMetric).collect(Collectors.toList()))
                    .isEqualTo(metrics);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }*/

    @ParameterizedTest
    @CsvSource({
        "availability, true, 1",
        "deployment-role, true, 20",
        "nonexistent, false, -1"
    })
    void findByMetric(String metric, boolean exists, long id, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByMetric(sessionManager, metric))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getMetricId()).isEqualTo(id);
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
        "availability, true, 1",
        "deployment-role, false, 20",
        "nonexistent, false, -1"
    })
    void findByMetricAndIsSLO(String metric, boolean exists, long id, VertxTestContext testContext) {
        // swap with findAllBySLO
        smProvider.withTransactionMaybe(sessionManager -> repository.findByMetric(sessionManager, metric))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getMetricId()).isEqualTo(id);
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
