package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceDeploymentRepositoryTest extends DatabaseTest {

    private final ServiceDeploymentRepository repository = new ServiceDeploymentRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, true, accountDefault);
            K8sServiceType k8sStNodePort = TestServiceProvider.createK8sServiceType(1L);
            K8sServiceType k8sStNoSvc = TestServiceProvider.createK8sServiceType(3L);
            ServiceType st1 = TestServiceProvider.createServiceTyp(2L, "notype");
            Service s1 = TestServiceProvider.createService(null, st1, "soo1", "latest", k8sStNodePort,
                List.of("80:8080"), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            Service s2 = TestServiceProvider.createService(null, st1, "soo2", "latest", k8sStNoSvc,
                List.of(), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(3L, "edge");
            Platform p1 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg2);
            SubResource sr1 = TestResourceProvider.createSubResourceWithoutMVs(null, "r3", (MainResource) r1);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            Metric mHostname = TestMetricProvider.createMetric(21L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mHostname, sr1, "node1");
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
            ServiceDeployment sd1 = TestServiceProvider.createServiceDeployment(null, s1, r1, true, d1,
                rds1);
            ServiceDeployment sd2 = TestServiceProvider.createServiceDeployment(null, s2, sr1, true, d1,
                rds2);
            ServiceDeployment sd3 = TestServiceProvider.createServiceDeployment(null, s1, r2, true, d1,
                rds3);
            ServiceDeployment sd4 = TestServiceProvider.createServiceDeployment(null, s1, r1, true, d2,
                rds4);
            ServiceDeployment sd5 = TestServiceProvider.createServiceDeployment(null, s2, r2, true, d2,
                rds5);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(s1))
                .flatMap(res -> sessionManager.persist(s2))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(sr1))
                .flatMap(res -> sessionManager.persist(mv1))
                .flatMap(res -> sessionManager.persist(mv2))
                .flatMap(res -> sessionManager.persist(sd1))
                .flatMap(res -> sessionManager.persist(sd2))
                .flatMap(res -> sessionManager.persist(sd3))
                .flatMap(res -> sessionManager.persist(sd4))
                .flatMap(res -> sessionManager.persist(sd5));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    private static Stream<Arguments> provideFindAllByDeploymentId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L, 3L), List.of(1L, 2L, 1L),
                List.of("notype", "notype", "notype"), List.of("NodePort", "NoService", "NodePort"),
                List.of("r1", "r3", "r2"), List.of(List.of("availability"), List.of("hostname"), List.of()),
                List.of(List.of("availability"), List.of("availability"), List.of()),
                List.of("k8s", "k8s", "k8s"), List.of("container", "container", "container"),
                List.of("us-east-1", "us-east-1", "edge"), List.of("aws", "aws", "custom-edge"),
                List.of("cloud", "cloud", "edge"), List.of("NEW", "ERROR", "DEPLOYED")),
            Arguments.of(2L, List.of(4L, 5L), List.of(1L, 2L),
                List.of("notype", "notype"), List.of("NodePort", "NoService"), List.of("r1", "r2"),
                List.of(List.of("availability"), List.of()), List.of(List.of("availability"), List.of()),
                List.of("k8s", "k8s"), List.of("container", "container"), List.of("us-east-1", "edge"),
                List.of("aws", "custom-edge"), List.of("cloud", "edge"), List.of("TERMINATING", "TERMINATED")),
            Arguments.of(3L, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByDeploymentId")
    void findAllByDeploymentId(long deploymentId, List<Long> serviceDeploymentIds, List<Long> serviceIds,
            List<String> serviceTypes, List<String> k8sServiceTypes, List<String> resources,
            List<List<String>> resourceMetrics, List<List<String>> mainMetrics, List<String> platforms,
            List<String> resourceTypes, List<String> regions, List<String> providers, List<String> environments,
            List<String> statusList, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByDeploymentId(sessionManager, deploymentId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(ResourceDeployment::getResourceDeploymentId)
                    .collect(Collectors.toList())).isEqualTo(serviceDeploymentIds);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getService().getServiceId())
                    .collect(Collectors.toList())).isEqualTo(serviceIds);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getService().getServiceType()
                    .getName()).collect(Collectors.toList())).isEqualTo(serviceTypes);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getService().getK8sServiceType()
                    .getName()).collect(Collectors.toList())).isEqualTo(k8sServiceTypes);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getName())
                    .collect(Collectors.toList())).isEqualTo(resources);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMetricValues()
                        .stream().map(mv -> mv.getMetric().getMetric()).collect(Collectors.toList()))
                    .collect(Collectors.toList())).isEqualTo(resourceMetrics);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMain().
                        getMetricValues().stream().map(mv -> mv.getMetric().getMetric()).collect(Collectors.toList()))
                    .collect(Collectors.toList())).isEqualTo(mainMetrics);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMain()
                    .getPlatform().getPlatform()).collect(Collectors.toList())).isEqualTo(platforms);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMain()
                    .getPlatform().getResourceType().getResourceType()).collect(Collectors.toList()))
                    .isEqualTo(resourceTypes);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMain()
                    .getRegion().getName()).collect(Collectors.toList())).isEqualTo(regions);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMain()
                    .getRegion().getResourceProvider().getProvider()).collect(Collectors.toList()))
                    .isEqualTo(providers);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getResource().getMain()
                    .getRegion().getResourceProvider().getEnvironment().getEnvironment()).collect(Collectors.toList()))
                    .isEqualTo(environments);
                assertThat(result.stream().map(serviceDeployment -> serviceDeployment.getStatus().getStatusValue())
                    .collect(Collectors.toList())).isEqualTo(statusList);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 1, NEW, 1",
        "1, 2, 1, NEW, 0",
        "1, 1, 2, NEW, 0",
        "1, 3, 1, NEW, 0",
        "1, 2, 1, ERROR, 1",
        "1, 3, 1, DEPLOYED, 1",
        "2, 1, 1, TERMINATING, 0",
        "2, 4, 1, TERMINATING, 0",
        "2, 4, 2, TERMINATING, 1",
        "3, 1, 1, NEW, 0",
    })
    void countByDeploymentStatus(long deploymentId, long resourceDeploymentId, long accountId, String status, int count,
            VertxTestContext testContext) {
        DeploymentStatusValue statusValue = DeploymentStatusValue.valueOf(status);
        smProvider.withTransactionSingle(sessionManager -> repository
                .countByDeploymentStatus(sessionManager, deploymentId, resourceDeploymentId, accountId, statusValue))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(count);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
