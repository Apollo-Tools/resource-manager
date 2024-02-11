package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link MetricRepository} class.
 *
 * @author matthi-g
 */
public class MetricRepositoryTest extends DatabaseTest {

    private final MetricRepository repository = new MetricRepository();

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


    @Test
    void findAllBySLOs(VertxTestContext testContext) {
        ServiceLevelObjective slo1 = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.EQ,
            0.99);
        ServiceLevelObjective slo2 = TestDTOProvider.createServiceLevelObjective("openfaas-user", ExpressionType.EQ,
            "user");
        ServiceLevelObjective slo3 = TestDTOProvider.createServiceLevelObjective("notexisting", ExpressionType.EQ,
           false);
        smProvider.withTransactionSingle(sessionManager -> repository.findAllBySLOs(sessionManager,
                List.of(slo1, slo2, slo3)))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(1);
                assertThat(result.get(0).getMetricId()).isEqualTo(1L);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
