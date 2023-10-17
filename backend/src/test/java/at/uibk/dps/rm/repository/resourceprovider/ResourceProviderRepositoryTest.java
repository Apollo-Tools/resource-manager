package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ResourceProviderRepository} class.
 *
 * @author matthi-g
 */
public class ResourceProviderRepositoryTest extends DatabaseTest {

    private final ResourceProviderRepository repository = new ResourceProviderRepository();

    @ParameterizedTest
    @CsvSource({
        "1, true, cloud",
        "4, true, cloud",
        "5, true, edge",
    })
    void findByRegionIdAndPlatformId(long providerId, boolean exists, String environment,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndFetch(sessionManager, providerId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getProviderId()).isEqualTo(providerId);
                    assertThat(result.getEnvironment().getEnvironment()).isEqualTo(environment);
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
    void findAllAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.get(0).getProviderId()).isEqualTo(1L);
                assertThat(result.get(0).getEnvironment().getEnvironment()).isEqualTo("cloud");
                assertThat(result.get(1).getProviderId()).isEqualTo(4L);
                assertThat(result.get(1).getEnvironment().getEnvironment()).isEqualTo("cloud");
                assertThat(result.get(2).getProviderId()).isEqualTo(5L);
                assertThat(result.get(2).getEnvironment().getEnvironment()).isEqualTo("edge");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
