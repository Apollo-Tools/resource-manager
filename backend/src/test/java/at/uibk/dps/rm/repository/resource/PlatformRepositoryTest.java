package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link PlatformRepository} class.
 *
 * @author matthi-g
 */
public class PlatformRepositoryTest extends DatabaseTest {

    private final PlatformRepository repository = new PlatformRepository();

    @Test
    void findAllAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(4);
                assertThat(result.get(0).getPlatformId()).isEqualTo(1L);
                assertThat(result.get(0).getResourceType().getResourceType()).isEqualTo("faas");
                assertThat(result.get(1).getPlatformId()).isEqualTo(2L);
                assertThat(result.get(1).getResourceType().getResourceType()).isEqualTo("faas");
                assertThat(result.get(2).getPlatformId()).isEqualTo(3L);
                assertThat(result.get(2).getResourceType().getResourceType()).isEqualTo("faas");
                assertThat(result.get(3).getPlatformId()).isEqualTo(4L);
                assertThat(result.get(3).getResourceType().getResourceType()).isEqualTo("container");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
