package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceDeploymentRepositoryTest extends DatabaseTest {

    private final ResourceDeploymentRepository repository = new ResourceDeploymentRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            Runtime rtPython = TestFunctionProvider.createRuntime(1L);
            FunctionType ft1 = TestFunctionProvider.createFunctionType(1L, "notype");
            Function f1 = TestFunctionProvider.createFunction(null, ft1, "foo1",
                "def main():\n  print()\n", rtPython, false, 300, 1024, true, accountAdmin);
            Function f2 = TestFunctionProvider.createFunction(null, ft1, "foo2",
                "file123455", rtPython, true, 300, 1024, true, accountAdmin);
            K8sServiceType k8sStNoSvc = TestServiceProvider.createK8sServiceType(3L);
            ServiceType st1 = TestServiceProvider.createServiceTyp(2L, "notype");
            Service s1 = TestServiceProvider.createService(null, st1, "soo1", "latest", k8sStNoSvc,
                List.of("80:8080"), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            Service s2 = TestServiceProvider.createService(null, st1, "soo2", "latest", k8sStNoSvc,
                List.of(), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(3L, "edge");
            Platform p1 = TestPlatformProvider.createPlatformContainer(1L, "lambda");
            Platform p2 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p2, reg2);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            ResourceDeploymentStatus rds1 = TestDeploymentProvider.createResourceDeploymentStatus(1L,
                DeploymentStatusValue.NEW);
            ResourceDeploymentStatus rds2 = TestDeploymentProvider.createResourceDeploymentStatus(2L,
                DeploymentStatusValue.ERROR);
            ResourceDeploymentStatus rds3 = TestDeploymentProvider.createResourceDeploymentStatus(3L,
                DeploymentStatusValue.DEPLOYED);
            ResourceDeploymentStatus rds4 = TestDeploymentProvider.createResourceDeploymentStatus(4L,
                DeploymentStatusValue.TERMINATING);
            ResourceDeploymentStatus rds5 = TestDeploymentProvider.createResourceDeploymentStatus(5L,
                DeploymentStatusValue.TERMINATED);
            FunctionDeployment fd1 = TestFunctionProvider.createFunctionDeployment(null, f1, r1, true, d1,
                rds1);
            FunctionDeployment fd2 = TestFunctionProvider.createFunctionDeployment(null, f2, r1, true, d1,
                rds2);
            FunctionDeployment fd3 = TestFunctionProvider.createFunctionDeployment(null, f1, r1, true, d2,
                rds3);
            ServiceDeployment sd1 = TestServiceProvider.createServiceDeployment(null, s1, r2, true, d1,
                rds4);
            ServiceDeployment sd2 = TestServiceProvider.createServiceDeployment(null, s2, r2, true, d2,
                rds5);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(f1))
                .flatMap(res -> sessionManager.persist(f2))
                .flatMap(res -> sessionManager.persist(s1))
                .flatMap(res -> sessionManager.persist(s2))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(mv1))
                .flatMap(res -> sessionManager.persist(fd1))
                .flatMap(res -> sessionManager.persist(fd2))
                .flatMap(res -> sessionManager.persist(fd3))
                .flatMap(res -> sessionManager.persist(sd1))
                .flatMap(res -> sessionManager.persist(sd2));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }



    private static Stream<Arguments> provideFindAllByDeploymentId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L, 4L), List.of("NEW", "ERROR", "TERMINATING")),
            Arguments.of(2L, List.of(3L, 5L), List.of("DEPLOYED", "TERMINATED")),
            Arguments.of(3L, List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByDeploymentId")
    void findAllByDeploymentId(long deploymentId, List<Long> resourceDeploymentId, List<String> statusList,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByDeploymentIdAndFetch(sessionManager, deploymentId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(ResourceDeployment::getResourceDeploymentId)
                    .collect(Collectors.toList())).isEqualTo(resourceDeploymentId);
                assertThat(result.stream().map(resourceDeployment -> resourceDeployment.getStatus().getStatusValue())
                    .collect(Collectors.toList())).isEqualTo(statusList);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, localhost",
        "2, localhost123",
        "4, localhost4"
    })
    void updateTriggerUrl(long resourceDeploymentId, String triggerUrl, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .updateTriggerUrl(sessionManager, resourceDeploymentId, triggerUrl)
                .andThen(Maybe.defer(() -> sessionManager.find(ResourceDeployment.class, resourceDeploymentId))))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getTriggerUrl()).isEqualTo(triggerUrl);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, NEW",
        "1, ERROR",
        "1, DEPLOYED",
        "1, TERMINATING",
        "1, TERMINATED",
        "2, NEW"
    })
    void updateDeploymentStatusByDeploymentId(long deploymentId, String status, VertxTestContext testContext) {
        DeploymentStatusValue statusValue = DeploymentStatusValue.valueOf(status);
        smProvider.withTransactionSingle(sessionManager -> repository
                .updateDeploymentStatusByDeploymentId(sessionManager, deploymentId, statusValue)
                .andThen(Single.defer(() -> repository.findAllByDeploymentIdAndFetch(sessionManager, deploymentId))))
            .subscribe(result -> testContext.verify(() -> {
                for (ResourceDeployment rd : result) {
                    assertThat(rd.getStatus().getStatusValue()).isEqualTo(status);
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
