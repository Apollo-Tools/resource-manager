package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link DeploymentRepositoryTest} class.
 *
 * @author matthi-g
 */
public class DeploymentRepositoryTest extends DatabaseTest {

    private final DeploymentRepository repository = new DeploymentRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, accountAdmin.getAccountId(), "e1", true);
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Deployment d1 = TestDeploymentProvider.createDeployment(null, accountAdmin, e1);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, accountAdmin, e1);
            Deployment d3 = TestDeploymentProvider.createDeployment(null, accountDefault);
            ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
            ResourceDeploymentStatus rdsNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
            ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(null, d1, r1, rdsNew);
            ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(null, d2, r1, rdsDeployed);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(d3))
                .flatMap(res -> sessionManager.persist(e1))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(rd1))
                .flatMap(res -> sessionManager.persist(rd2));
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

    @Test
    void findAllActiveWithAlerting(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllActiveWithAlerting)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Deployment::getDeploymentId).collect(Collectors.toList()))
                    .isEqualTo(List.of(2L));
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
            }), throwable -> testContext.verify(() -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            }));
    }

    @Test
    void findByIdAndAccountId(VertxTestContext testContext) {
        long startTimestamp = System.currentTimeMillis();
        smProvider.withTransactionCompletable(sessionManager -> sessionManager.find(Deployment.class, 1L)
            .flatMapCompletable(deployment -> {
                if (deployment.getFinishedAt() != null) {
                    return Completable.error(new RuntimeException("finish timestamp not null"));
                }
                return repository.setDeploymentFinishedTime(sessionManager, 1L);
            }))
            .andThen(Maybe.defer(() -> smProvider
                .withTransactionMaybe(sessionManager -> sessionManager.find(Deployment.class, 1L))))
            .subscribe(result -> testContext.verify(() -> {
                long endTimestamp = System.currentTimeMillis();
                assertThat(result.getFinishedAt()).isNotNull();
                assertThat(result.getFinishedAt()).isBetween(new Date(startTimestamp), new Date(endTimestamp));
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
