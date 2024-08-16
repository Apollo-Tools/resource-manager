package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.misc.DateHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link DeploymentRepositoryTest} class.
 *
 * @author matthi-g
 */
@ExtendWith(MockitoExtension.class)
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
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg1);
            Deployment d1 = TestDeploymentProvider.createDeployment(null, accountAdmin, e1);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, accountAdmin, e1);
            Deployment d3 = TestDeploymentProvider.createDeployment(null, accountDefault);
            Deployment d4 = TestDeploymentProvider.createDeployment(null, accountDefault);
            Deployment d5 = TestDeploymentProvider.createDeployment(null, accountDefault);
            ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
            ResourceDeploymentStatus rdsNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
            ResourceDeploymentStatus rdsError = TestDeploymentProvider.createResourceDeploymentStatusError();
            ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(null, d1, r1, rdsNew);
            ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(null, d2, r1, rdsDeployed);
            Runtime rtPython = TestFunctionProvider.createRuntime(1L);
            FunctionType ft1 = TestFunctionProvider.createFunctionType(1L, "notype");
            Function f1 = TestFunctionProvider.createFunction(null, ft1, "foo1",
                "def main():\n  print()\n", rtPython, false, 300, 1024, true, accountAdmin);
            ResourceDeployment rd3 = TestFunctionProvider.createFunctionDeployment(null, f1, r1, d3, rdsError);
            K8sServiceType k8sStNodePort = TestServiceProvider.createK8sServiceType(1L);
            ServiceType st1 = TestServiceProvider.createServiceTyp(2L, "notype");
            Service s1 = TestServiceProvider.createService(null, st1, "soo1", "latest", k8sStNodePort,
                List.of("80:8080"), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            ResourceDeployment rd4 = TestServiceProvider.createServiceDeployment(null, s1, r1, d4, rdsError);
            ResourceDeployment rd5 = TestFunctionProvider.createFunctionDeployment(null, f1, r2, d5, rdsDeployed);
            ResourceDeployment rd6 = TestServiceProvider.createServiceDeployment(null, s1, r2, d5, rdsDeployed);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(d3))
                .flatMap(res -> sessionManager.persist(d4))
                .flatMap(res -> sessionManager.persist(d5))
                .flatMap(res -> sessionManager.persist(e1))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(f1))
                .flatMap(res -> sessionManager.persist(s1))
                .flatMap(res -> sessionManager.persist(rd1))
                .flatMap(res -> sessionManager.persist(rd2))
                .flatMap(res -> sessionManager.persist(rd3))
                .flatMap(res -> sessionManager.persist(rd4))
                .flatMap(res -> sessionManager.persist(rd5))
                .flatMap(res -> sessionManager.persist(rd6));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    private static Stream<Arguments> provideFindAllByAccountId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L)),
            Arguments.of(2L, List.of(3L, 4L, 5L)),
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
        "7, 1, false",
        "1, 2, false",
        "2, 2, false",
        "3, 2, true",
        "4, 2, true",
        "7, 2, false",
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

    private static Stream<Arguments> provideFindAllWithErrorStateByIds() {
        Calendar realCalendar = Calendar.getInstance();
        realCalendar.add(Calendar.MINUTE, -15);
        Date nowLast15Mins = realCalendar.getTime();
        realCalendar.add(Calendar.MINUTE, 30);
        Date futureLast15Mins = realCalendar.getTime();
        return Stream.of(
            Arguments.of(List.of(1L, 2L, 3L, 4L, 5L), List.of(3L, 4L), futureLast15Mins),
            Arguments.of(List.of(1L, 2L, 3L, 4L, 5L), List.of(), nowLast15Mins),
            Arguments.of(List.of(1L, 2L, 5L), List.of(), futureLast15Mins),
            Arguments.of(List.of(3L, 4L), List.of(3L, 4L), futureLast15Mins),
            Arguments.of(List.of(3L, 4L), List.of(), nowLast15Mins),
            Arguments.of(List.of(3L), List.of(3L), futureLast15Mins),
            Arguments.of(List.of(4L), List.of(4L), futureLast15Mins),
            Arguments.of(List.of(), List.of(), futureLast15Mins),
            Arguments.of(List.of(), List.of(), nowLast15Mins)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllWithErrorStateByIds")
    void findAllWithErrorStateByIds(List<Long> ids, List<Long> resultDeployments, Date last15Mins,
                                    VertxTestContext testContext) {
            smProvider.withTransactionSingle(sessionManager -> {
                    try (MockedStatic<DateHelper> mockedDateHelper = mockStatic(DateHelper.class)) {
                        mockedDateHelper.when(() -> DateHelper.getDate(-15)).thenReturn(last15Mins);
                        return repository.findAllWithErrorStateByIds(sessionManager, ids);
                    }
                })
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.stream().map(Deployment::getDeploymentId)).isEqualTo(resultDeployments);
                    testContext.completeNow();
                }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void setDeploymentFinishedTime(VertxTestContext testContext) {
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
