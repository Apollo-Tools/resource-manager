package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Maybe;
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

/**
 * Implements tests for the {@link FunctionDeploymentRepository} class.
 *
 * @author matthi-g
 */
public class FunctionDeploymentRepositoryTest extends DatabaseTest {

    private final FunctionDeploymentRepository repository = new FunctionDeploymentRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, accountAdmin);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, accountDefault);
            Runtime rtPython = TestFunctionProvider.createRuntime(1L);
            FunctionType ft1 = TestFunctionProvider.createFunctionType(1L, "notype");
            Function f1 = TestFunctionProvider.createFunction(null, ft1, "foo1",
                "def main():\n  print()\n", rtPython, false, 300, 1024, true, accountAdmin);
            Function f2 = TestFunctionProvider.createFunction(null, ft1, "foo2",
                "file123455", rtPython, true, 300, 1024, true, accountAdmin);
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(3L, "edge");
            Platform p1 = TestPlatformProvider.createPlatformContainer(1L, "lambda");
            Platform p2 = TestPlatformProvider.createPlatformContainer(3L, "openfaas");
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
            FunctionDeployment fd1 = TestFunctionProvider.createFunctionDeployment(null, f1, r1, d1,
                rds1);
            FunctionDeployment fd2 = TestFunctionProvider.createFunctionDeployment(null, f2, r1, d1,
                rds2);
            FunctionDeployment fd3 = TestFunctionProvider.createFunctionDeployment(null, f1, r2, d1,
                rds3);
            FunctionDeployment fd4 = TestFunctionProvider.createFunctionDeployment(null, f1, r1, d2,
                rds4);
            FunctionDeployment fd5 = TestFunctionProvider.createFunctionDeployment(null, f2, r2, d2,
                rds5);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(f1))
                .flatMap(res -> sessionManager.persist(f2))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(mv1))
                .flatMap(res -> sessionManager.persist(fd1))
                .flatMap(res -> sessionManager.persist(fd2))
                .flatMap(res -> sessionManager.persist(fd3))
                .flatMap(res -> sessionManager.persist(fd4))
                .flatMap(res -> sessionManager.persist(fd5));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    private static Stream<Arguments> provideFindAllByDeploymentId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L, 3L), List.of(1L, 2L, 1L), List.of("notype", "notype", "notype"),
                List.of("python3.8", "python3.8", "python3.8"), List.of("r1", "r1", "r2"),
                List.of(List.of("availability"), List.of("availability"), List.of()),
                List.of("lambda", "lambda", "openfaas"), List.of("faas", "faas", "faas"),
                List.of("us-east-1", "us-east-1", "edge"), List.of("aws", "aws", "custom-edge"),
                List.of("cloud", "cloud", "edge"), List.of("NEW", "ERROR", "DEPLOYED")),
            Arguments.of(2L, List.of(4L, 5L), List.of(1L, 2L), List.of("notype", "notype"),
                List.of("python3.8", "python3.8"), List.of("r1", "r2"),
                List.of(List.of("availability"), List.of()), List.of("lambda", "openfaas"), List.of("faas", "faas"),
                List.of("us-east-1", "edge"), List.of("aws", "custom-edge"), List.of("cloud", "edge"),
                List.of("TERMINATING", "TERMINATED")),
            Arguments.of(3L, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByDeploymentId")
    void findAllByDeploymentId(long deploymentId, List<Long> functionDeploymentIds, List<Long> functionIds,
            List<String> serviceTypes, List<String> runtimes, List<String> resources, List<List<String>> metrics,
            List<String> platforms, List<String> resourceTypes, List<String> regions, List<String> providers,
            List<String> environments, List<String> statusList, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByDeploymentId(sessionManager, deploymentId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(ResourceDeployment::getResourceDeploymentId)
                    .collect(Collectors.toList())).isEqualTo(functionDeploymentIds);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getFunction().getFunctionId())
                    .collect(Collectors.toList())).isEqualTo(functionIds);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getFunction().getFunctionType()
                    .getName()).collect(Collectors.toList())).isEqualTo(serviceTypes);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getFunction().getRuntime()
                    .getName()).collect(Collectors.toList())).isEqualTo(runtimes);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getName())
                    .collect(Collectors.toList())).isEqualTo(resources);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getMetricValues()
                        .stream().map(mv -> mv.getMetric().getMetric()).collect(Collectors.toList()))
                    .collect(Collectors.toList())).isEqualTo(metrics);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getMain()
                    .getPlatform().getPlatform()).collect(Collectors.toList())).isEqualTo(platforms);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getMain()
                    .getPlatform().getResourceType().getResourceType()).collect(Collectors.toList()))
                    .isEqualTo(resourceTypes);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getMain()
                    .getRegion().getName()).collect(Collectors.toList())).isEqualTo(regions);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getMain()
                    .getRegion().getResourceProvider().getProvider()).collect(Collectors.toList()))
                    .isEqualTo(providers);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getResource().getMain()
                    .getRegion().getResourceProvider().getEnvironment().getEnvironment()).collect(Collectors.toList()))
                    .isEqualTo(environments);
                assertThat(result.stream().map(functionDeployment -> functionDeployment.getStatus().getStatusValue())
                    .collect(Collectors.toList())).isEqualTo(statusList);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, localhost, awsurl",
        "2, localhost123, edgeurl",
        "4, localhost4, url"
    })
    void updateTriggerUrls(long resourceDeploymentId, String triggerUrl, String directTriggerUrl,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .updateTriggerUrls(sessionManager, resourceDeploymentId, triggerUrl, directTriggerUrl)
                .andThen(Maybe.defer(() -> sessionManager.find(FunctionDeployment.class, resourceDeploymentId))))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getRmTriggerUrl()).isEqualTo(triggerUrl);
                assertThat(result.getDirectTriggerUrl()).isEqualTo(directTriggerUrl);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }


    @ParameterizedTest
    @CsvSource({
        "1, 1, true, NEW",
        "1, 2, false, NEW",
        "4, 1, false, NEW",
        "4, 2, true, TERMINATING",
        "10, 1, false, NEW",
    })
    void findByIdAndAccountId(long resourceDeploymentId, long accountId, boolean exists, String status,
            VertxTestContext testContext) {
        DeploymentStatusValue statusValue = DeploymentStatusValue.valueOf(status);
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, resourceDeploymentId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getResourceDeploymentId()).isEqualTo(resourceDeploymentId);
                    assertThat(DeploymentStatusValue.fromDeploymentStatus(result.getStatus())).isEqualTo(statusValue);
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
