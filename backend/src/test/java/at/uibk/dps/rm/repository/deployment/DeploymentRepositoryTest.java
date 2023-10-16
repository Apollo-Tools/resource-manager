package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DeploymentRepositoryTest extends DatabaseTest {

    private final DeploymentRepository repository = new DeploymentRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            Deployment d3 = TestDeploymentProvider.createDeployment(null, true, accountDefault);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(d3));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    private static Stream<Arguments> provideFindAllByAccountId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L)),
            Arguments.of(2L, List.of(3L)),
            Arguments.of(3L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByAccountId")
    void findAllByAccountId(long accountId, List<Long> deploymentIds, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByAccountId(sessionManager, accountId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Deployment::getDeploymentId).collect(Collectors.toList()))
                    .isEqualTo(deploymentIds);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
    
    @ParameterizedTest
    @CsvSource({
        "1, 1, true",
        "2, 1, true",
        "3, 1, false",
        "4, 1, false",
        "1, 2, false",
        "2, 2, false",
        "3, 2, true",
        "4, 2, false",
        "1, 3, false",
    })
    void findByIdAndAccountId(long deploymentId, long accountId, boolean exists, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, deploymentId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getDeploymentId()).isEqualTo(deploymentId);
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
