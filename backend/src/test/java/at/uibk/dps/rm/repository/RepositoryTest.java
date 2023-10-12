package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.dto.account.RoleEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.testutil.DatabaseTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryTest extends DatabaseTest {

    private final ConcreteRepository repository = new ConcreteRepository();

    /**
     * Implements a concrete class of the {@link ValidationHandler} class.
     */
    static class ConcreteRepository extends Repository<Role> {
        /**
         * Create an instance.
         */
        protected ConcreteRepository() {
            super(Role.class);
        }
    }

    @Test
    void findByIdAndAccountId(VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, 1L, 1L))
            .subscribe(result -> testContext.failNow("methods did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable.getCause()).isInstanceOf(UnsupportedOperationException.class);
                    testContext.completeNow();
                }));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAll)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getRoleId()).isEqualTo(1L);
                assertThat(result.get(1).getRoleId()).isEqualTo(2L);
                assertThat(result.get(0).getRole()).isEqualTo(RoleEnum.ADMIN.getValue());
                assertThat(result.get(1).getRole()).isEqualTo(RoleEnum.DEFAULT.getValue());
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByAccountId(sessionManager, 1L))
            .subscribe(result -> testContext.failNow("methods did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable.getCause()).isInstanceOf(UnsupportedOperationException.class);
                    testContext.completeNow();
                }));
    }
}
