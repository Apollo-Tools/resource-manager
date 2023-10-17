package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceDeploymentStatusRepositoryTest extends DatabaseTest {

    private final ResourceDeploymentStatusRepository repository = new ResourceDeploymentStatusRepository();

    @ParameterizedTest
    @CsvSource({
        "NEW, true, 1",
        "ERROR, true, 2",
        "DEPLOYED, true, 3",
        "TERMINATING, true, 4",
        "TERMINATED, true, 5",
        "PAUSED, false, -1",
    })
    void findByIdAndFetch(String status, boolean exists, long statusId, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findOneByStatusValue(sessionManager, status))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getStatusId()).isEqualTo(statusId);
                    assertThat(result.getStatusValue()).isEqualTo(status);
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
