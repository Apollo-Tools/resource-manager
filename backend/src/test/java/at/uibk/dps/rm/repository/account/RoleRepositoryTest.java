package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.testutil.DatabaseTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleRepositoryTest extends DatabaseTest {

    private final RoleRepository repository = new RoleRepository();

    @ParameterizedTest
    @CsvSource({
        "admin, true, 1",
        "default, true, 2",
        "superuser, false, -1"
    })
    void findByRoleName(String role, boolean exists, long id, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByRoleName(sessionManager, role))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getRoleId()).isEqualTo(id);
                    assertThat(result.getRole()).isEqualTo(role);
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
