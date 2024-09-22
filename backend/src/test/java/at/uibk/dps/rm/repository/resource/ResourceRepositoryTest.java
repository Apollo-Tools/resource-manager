package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.resource.*;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ResourceRepository} class.
 *
 * @author matthi-g
 */
public class ResourceRepositoryTest extends DatabaseTest {

    private final ResourceRepository repository = new ResourceRepository();

    private static final List<Long> allResourceIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionCompletable(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-2");
            Region reg3 = TestResourceProviderProvider.createRegion(3L, "edge");
            Region reg4 = TestResourceProviderProvider.createRegion(4L, "fog");
            Platform pLambda = TestPlatformProvider.createPlatformContainer(1L, "lambda");
            Platform pEC2 = TestPlatformProvider.createPlatformContainer(2L, "ec2");
            Platform pOpenfaas = TestPlatformProvider.createPlatformContainer(3L, "openfaas");
            Platform pK8s = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", pK8s, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", pK8s, reg4);
            Resource r3 = TestResourceProvider.createResource(null, "r3", pLambda, reg1);
            Resource r4 = TestResourceProvider.createResource(null, "r4", pEC2, reg1);
            Resource r5 = TestResourceProvider.createResource(null, "r5", pEC2, reg2);
            Resource r6 = TestResourceProvider.createResource(null, "r6", pOpenfaas, reg3);
            SubResource sr1 = TestResourceProvider.createSubResourceWithoutMVs(null, "r7", (MainResource) r1);
            SubResource sr2 = TestResourceProvider.createSubResourceWithoutMVs(null, "r8", (MainResource) r2);
            Metric mInstanceType = TestMetricProvider.createMetric(11L);
            Metric mClusterUrl = TestMetricProvider.createMetric(12L);
            Metric mExternalIp = TestMetricProvider.createMetric(16L);
            Metric mPrePullTimeout = TestMetricProvider.createMetric(17L);
            Metric mDeploymentRole = TestMetricProvider.createMetric(20L);
            Metric mBaseUrl = TestMetricProvider.createMetric(27L);
            Metric mMetricsPort = TestMetricProvider.createMetric(29L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mExternalIp, r1, "localhost");
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mClusterUrl, r1, "http://localhost:6443");
            MetricValue mv3 = TestMetricProvider.createMetricValue(null, mPrePullTimeout, r1, 2.0);
            MetricValue mv4 = TestMetricProvider.createMetricValue(null, mPrePullTimeout, r2, 2.0);
            MetricValue mv5 = TestMetricProvider.createMetricValue(null, mDeploymentRole, r3, "LabRole");
            MetricValue mv6 = TestMetricProvider.createMetricValue(null, mInstanceType, r4, "t2.medium");
            MetricValue mv7 = TestMetricProvider.createMetricValue(null, mBaseUrl, r6, "http://localhost");
            MetricValue mv8 = TestMetricProvider.createMetricValue(null, mMetricsPort, r6, 9100);
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1");
            Ensemble e2 = TestEnsembleProvider.createEnsemble(null, 2L, "e2");
            ResourceEnsemble re1 = TestEnsembleProvider.createResourceEnsemble(null, e1, r1);
            ResourceEnsemble re2 = TestEnsembleProvider.createResourceEnsemble(null, e1, r2);
            ResourceEnsemble re3 = TestEnsembleProvider.createResourceEnsemble(null, e2, sr1);
            Deployment d1 = TestDeploymentProvider.createDeployment(null, accountAdmin);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, accountAdmin);
            FunctionType ft1 = TestFunctionProvider.createFunctionType(1L, "notype");
            Runtime rtPython = TestFunctionProvider.createRuntime(1L);
            Function f1 = TestFunctionProvider.createFunction(null, ft1, "foo1", "def main():\n  print()\n",
                rtPython, false, 300, 1024, true, accountAdmin);
            Function f2 = TestFunctionProvider.createFunction(null, ft1, "foo2", "def main():\n  print()\n",
                rtPython, false, 300, 1024, true, accountAdmin);
            Function f3 = TestFunctionProvider.createFunction(null, ft1, "foo3", "def main():\n  print()\n",
                rtPython, false, 300, 1024, true, accountAdmin);
            Function f4 = TestFunctionProvider.createFunction(null, ft1, "foo4", "def main():\n  print()\n",
                rtPython, false, 300, 1024, true, accountAdmin);
            ResourceDeploymentStatus rdsNew = TestDeploymentProvider.createResourceDeploymentStatus(1L,
                DeploymentStatusValue.NEW);
            ResourceDeploymentStatus rdsError = TestDeploymentProvider.createResourceDeploymentStatus(2L,
                DeploymentStatusValue.ERROR);
            ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatus(3L,
                DeploymentStatusValue.DEPLOYED);
            ResourceDeploymentStatus rdsTerminating = TestDeploymentProvider.createResourceDeploymentStatus(4L,
                DeploymentStatusValue.TERMINATING);
            ResourceDeploymentStatus rdsTerminated = TestDeploymentProvider.createResourceDeploymentStatus(5L,
                DeploymentStatusValue.TERMINATED);
            ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(null, d1, r1, rdsDeployed);
            ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(null, d1, r3, rdsDeployed);
            ResourceDeployment rd3 = TestFunctionProvider.createFunctionDeployment(null, f1, r5, d1, rdsDeployed,
                "https://aws.com", 9100);
            ResourceDeployment rd4 = TestDeploymentProvider.createResourceDeployment(null, d1, sr2, rdsDeployed);
            ResourceDeployment rd5 = TestFunctionProvider.createFunctionDeployment(null, f1, r5, d2, rdsNew,
                "https://aws.com", 9100);
            ResourceDeployment rd6 = TestFunctionProvider.createFunctionDeployment(null, f2, r5, d2, rdsError,
                "https://aws.com", 9100);
            ResourceDeployment rd7 = TestFunctionProvider.createFunctionDeployment(null, f3, r5, d2, rdsTerminating,
                "https://aws.com", 9100);
            ResourceDeployment rd8 = TestFunctionProvider.createFunctionDeployment(null, f4, r5, d2, rdsTerminated,
                "https://aws.com", 9100);
            r1.setLockedByDeployment(d1);
            r3.setLockedByDeployment(d1);

            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(r3))
                .flatMap(res -> sessionManager.persist(r4))
                .flatMap(res -> sessionManager.persist(r5))
                .flatMap(res -> sessionManager.persist(r6))
                .flatMap(res -> sessionManager.persist(sr1))
                .flatMap(res -> sessionManager.persist(sr2))
                .flatMap(res -> sessionManager.persist(e1))
                .flatMap(res -> sessionManager.persist(e2))
                .flatMap(res -> sessionManager.persist(re1))
                .flatMap(res -> sessionManager.persist(re2))
                .flatMap(res -> sessionManager.persist(re3))
                .flatMapCompletable(res -> sessionManager.persist(new Function[]{f1, f2, f3, f4}))
                .andThen(Single.defer(() -> Single.just(1L)))
                .flatMapCompletable(res -> sessionManager.persist(new ResourceDeployment[]{rd1, rd2, rd3, rd4, rd5,
                    rd6, rd7, rd8}))
                .andThen(Single.defer(() -> Single.just(1L)))
                .flatMapCompletable(res -> sessionManager.persist(new MetricValue[]{mv1, mv2, mv3, mv4, mv5, mv6, mv7,
                    mv8}));
        }).blockingSubscribe(() -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "r1, true, 1",
        "r2, true, 2",
        "r3, true, 3",
        "r4, true, 4",
        "r5, true, 5",
        "r6, true, 6",
        "r7, false, -1",
        "r8, false, -1",
        "r99, false, -1"
    })
    void findByName(String name, boolean exists, long id, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByName(sessionManager, name))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getResourceId()).isEqualTo(id);
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

    private static Stream<Arguments> provideFindByIdAndFetch() {
        List<String> r1Metrics = List.of("external-ip", "cluster-url", "pre-pull-timeout");
        List<String> r2Metrics = List.of("pre-pull-timeout");
        List<String> r3Metrics = List.of("deployment-role");
        List<String> r4Metrics = List.of("instance-type");
        List<String> r6Metrics = List.of("base-url", "metrics-port");
        String rpAws = "aws";
        String rpCustomFog = "custom-fog";
        String rpCustomEdge = "custom-edge";
        String eEdge = "edge";
        String eCloud = "cloud";
        String eFog = "fog";
        return Stream.of(
            Arguments.of(1L, true, r1Metrics, r1Metrics, "us-east-1", rpAws, eCloud, PlatformEnum.K8S),
            Arguments.of(2L, true, r2Metrics, r2Metrics, "fog", rpCustomFog, eFog, PlatformEnum.K8S),
            Arguments.of(3L, true, r3Metrics,  r3Metrics, "us-east-1", rpAws, eCloud, PlatformEnum.LAMBDA),
            Arguments.of(4L, true, r4Metrics, r4Metrics, "us-east-1", rpAws, eCloud, PlatformEnum.EC2),
            Arguments.of(5L, true, List.of(), List.of(), "us-west-2", rpAws, eCloud, PlatformEnum.EC2),
            Arguments.of(6L, true, r6Metrics,  r6Metrics, "edge", rpCustomEdge, eEdge, PlatformEnum.OPENFAAS),
            Arguments.of(7L, true, List.of(),  r1Metrics, "us-east-1", rpAws, eCloud, PlatformEnum.K8S),
            Arguments.of(8L, true, List.of(), r2Metrics, "fog", rpCustomFog, eFog, PlatformEnum.K8S),
            Arguments.of(9L, false, List.of(),  List.of(), "", "", "", null),
            Arguments.of(99L, false, List.of(), List.of(), "", "", "", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindByIdAndFetch")
    void findByIdAndFetch(long resourceId, boolean exists, List<String> metrics, List<String> mainMetrics,
            String region, String resourceProvider, String environment, PlatformEnum platform,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, resourceId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getResourceId()).isEqualTo(resourceId);
                    assertThat(result.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toList())).isEqualTo(metrics);
                    assertThat(result.getMain().getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toList())).isEqualTo(mainMetrics);
                    assertThat(result.getMain().getRegion().getName()).isEqualTo(region);
                    assertThat(result.getMain().getRegion().getResourceProvider().getProvider())
                        .isEqualTo(resourceProvider);
                    assertThat(result.getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                        .isEqualTo(environment);
                    assertThat(result.getMain().getPlatform().getPlatform()).isEqualTo(platform.getValue());
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

    private static Stream<Arguments> provideFindAllMainResources() {
        return Stream.of(
            Arguments.of(List.of(1L, 2L, 3L, 4L, 5L, 6L),
                List.of("us-east-1", "fog", "us-east-1", "us-east-1", "us-west-2", "edge"),
                List.of(ResourceProviderEnum.AWS, ResourceProviderEnum.CUSTOM_FOG, ResourceProviderEnum.AWS,
                    ResourceProviderEnum.AWS, ResourceProviderEnum.AWS, ResourceProviderEnum.CUSTOM_EDGE),
                List.of("cloud", "fog", "cloud", "cloud", "cloud", "edge"),
                List.of(PlatformEnum.K8S, PlatformEnum.K8S, PlatformEnum.LAMBDA, PlatformEnum.EC2, PlatformEnum.EC2,
                    PlatformEnum.OPENFAAS),
                List.of(ResourceTypeEnum.CONTAINER, ResourceTypeEnum.CONTAINER, ResourceTypeEnum.FAAS,
                    ResourceTypeEnum.FAAS, ResourceTypeEnum.FAAS, ResourceTypeEnum.FAAS),
                List.of(7L, 8L, -1L, -1L, -1L, -1L))
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllMainResources")
    void findAllAndFetch(List<Long> ids, List<String> regions,
            List<ResourceProviderEnum> resourceProviders, List<String> environments, List<PlatformEnum> platforms,
            List<ResourceTypeEnum> resourceTypes, List<Long> subResourceIds, VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllMainResourcesAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(ids.size());
                for(int i = 0; i < result.size(); i++) {
                    MainResource resource = (MainResource) result.get(i);
                    assertThat(resource.getResourceId()).isEqualTo(ids.get(i));
                    assertThat(resource.getRegion().getName()).isEqualTo(regions.get(i));
                    assertThat(resource.getRegion().getResourceProvider().getProvider())
                        .isEqualTo(resourceProviders.get(i).getValue());
                    assertThat(resource.getRegion().getResourceProvider().getEnvironment().getEnvironment())
                        .isEqualTo(environments.get(i));
                    assertThat(resource.getPlatform().getPlatform()).isEqualTo(platforms.get(i).getValue());
                    assertThat(resource.getPlatform().getResourceType().getResourceType())
                        .isEqualTo(resourceTypes.get(i).getValue());
                    if (subResourceIds.get(i) != -1) {
                        assertThat(resource.getSubResources().get(0).getResourceId()).isEqualTo(subResourceIds.get(i));
                    }
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllMainResourcesByPlatform() {
        return Stream.of(
            Arguments.of(PlatformEnum.EC2.getValue(), List.of(4L, 5L), List.of(Set.of("instance-type"), Set.of())),
            Arguments.of(PlatformEnum.K8S.getValue(), List.of(1L, 2L),
                List.of(Set.of("pre-pull-timeout", "external-ip", "cluster-url"), Set.of("pre-pull-timeout"))),
            Arguments.of(PlatformEnum.LAMBDA.getValue(), List.of(3L), List.of(Set.of("deployment-role"), Set.of())),
            Arguments.of(PlatformEnum.OPENFAAS.getValue(), List.of(6L), List.of(Set.of("base-url", "metrics-port"),
                Set.of())),
            Arguments.of("unknown", List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllMainResourcesByPlatform")
    void findAllMainResourcesByPlatform(String platform, List<Long> ids, List<Set<String>> metrics,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
            .findAllMainResourcesByPlatform(sessionManager, platform))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(ids.size());
                for(int i = 0; i < result.size(); i++) {
                    MainResource resource = (MainResource) result.get(i);
                    assertThat(resource.getResourceId()).isEqualTo(ids.get(i));
                    assertThat(resource.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                            .collect(Collectors.toSet()))
                        .isEqualTo(metrics.get(i));
                }
                testContext.completeNow();
            }));
    }

    private static Stream<Arguments> provideFindAll() {
        return Stream.of(
            Arguments.of(allResourceIds, List.of(Set.of("pre-pull-timeout", "external-ip",
                        "cluster-url"), Set.of("pre-pull-timeout"), Set.of("deployment-role"), Set.of("instance-type"),
                    Set.of(), Set.of("base-url", "metrics-port"), Set.of(), Set.of()),
                List.of("us-east-1", "fog", "us-east-1", "us-east-1", "us-west-2", "edge", "us-east-1",
                    "fog"),
                List.of(ResourceProviderEnum.AWS, ResourceProviderEnum.CUSTOM_FOG, ResourceProviderEnum.AWS,
                    ResourceProviderEnum.AWS, ResourceProviderEnum.AWS, ResourceProviderEnum.CUSTOM_EDGE,
                    ResourceProviderEnum.AWS, ResourceProviderEnum.CUSTOM_FOG),
                List.of("cloud", "fog", "cloud", "cloud", "cloud", "edge", "cloud", "fog"),
                List.of(PlatformEnum.K8S, PlatformEnum.K8S, PlatformEnum.LAMBDA, PlatformEnum.EC2, PlatformEnum.EC2,
                    PlatformEnum.OPENFAAS, PlatformEnum.K8S, PlatformEnum.K8S),
                List.of(ResourceTypeEnum.CONTAINER, ResourceTypeEnum.CONTAINER, ResourceTypeEnum.FAAS,
                    ResourceTypeEnum.FAAS, ResourceTypeEnum.FAAS, ResourceTypeEnum.FAAS, ResourceTypeEnum.CONTAINER,
                    ResourceTypeEnum.CONTAINER),
                List.of(-1, -1, -1, -1, -1, -1,  1L, 2L))
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAll")
    void findAllMainAndSubResourcesAndFetch(List<Long> ids, List<Set<String>> metrics, List<String> regions,
            List<ResourceProviderEnum> resourceProviders, List<String> environments, List<PlatformEnum> platforms,
            List<ResourceTypeEnum> resourceTypes, List<Long> mainResourceIds, VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllMainAndSubResourcesAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(ids.size());
                for(int i = 0; i < result.size(); i++) {
                    Resource resource = result.get(i);
                    assertThat(resource.getResourceId()).isEqualTo(ids.get(i));
                    assertThat(resource.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                            .collect(Collectors.toSet()))
                        .isEqualTo(metrics.get(i));
                    MainResource mainResource;
                    if (resource instanceof MainResource) {
                        mainResource = (MainResource) resource;
                    } else {
                        mainResource = ((SubResource) resource).getMainResource();
                    }
                    assertThat(mainResource.getRegion().getName()).isEqualTo(regions.get(i));
                    assertThat(mainResource.getRegion().getResourceProvider().getProvider())
                        .isEqualTo(resourceProviders.get(i).getValue());
                    assertThat(mainResource.getRegion().getResourceProvider().getEnvironment().getEnvironment())
                        .isEqualTo(environments.get(i));
                    assertThat(mainResource.getPlatform().getPlatform()).isEqualTo(platforms.get(i).getValue());
                    assertThat(mainResource.getPlatform().getResourceType().getResourceType())
                        .isEqualTo(resourceTypes.get(i).getValue());
                    if (resource instanceof SubResource) {
                        assertThat(resource.getMain().getResourceId()).isEqualTo(mainResourceIds.get(i));
                    }
                }
                testContext.completeNow();
            }));
    }

    @ParameterizedTest
    @MethodSource("provideFindAll")
    void findAllBySLOsNoSLOs(List<Long> ids, List<Set<String>> metrics, List<String> regions,
                             List<ResourceProviderEnum> resourceProviders, List<String> environments, List<PlatformEnum> platforms,
                             List<ResourceTypeEnum> resourceTypes, List<Long> mainResourceIds, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByNonMVSLOs(sessionManager, List.of(),
                List.of(), List.of(), List.of(), List.of()))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(ids.size());
                for(int i = 0; i < result.size(); i++) {
                    Resource resource = result.get(i);
                    assertThat(resource.getResourceId()).isEqualTo(ids.get(i));
                    assertThat(resource.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toSet()))
                        .isEqualTo(metrics.get(i));
                    MainResource mainResource;
                    if (resource instanceof MainResource) {
                        mainResource = (MainResource) resource;
                    } else {
                        mainResource = ((SubResource) resource).getMainResource();
                    }
                    assertThat(mainResource.getRegion().getName()).isEqualTo(regions.get(i));
                    assertThat(mainResource.getRegion().getResourceProvider().getProvider())
                        .isEqualTo(resourceProviders.get(i).getValue());
                    assertThat(mainResource.getRegion().getResourceProvider().getEnvironment().getEnvironment())
                        .isEqualTo(environments.get(i));
                    assertThat(mainResource.getPlatform().getPlatform()).isEqualTo(platforms.get(i).getValue());
                    assertThat(mainResource.getPlatform().getResourceType().getResourceType())
                        .isEqualTo(resourceTypes.get(i).getValue());
                    if (resource instanceof SubResource) {
                        assertThat(resource.getMain().getResourceId()).isEqualTo(mainResourceIds.get(i));
                    }
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByNonMVSLOs() {
        List<Long> emptyList = List.of();
        return Stream.of(
            Arguments.of(List.of(1L), emptyList, emptyList, emptyList, emptyList,
                List.of(1L, 3L, 4L, 5L, 7L)),
            Arguments.of(List.of(2L), emptyList, emptyList, emptyList, emptyList,
                List.of(6L)),
            Arguments.of(List.of(1L, 2L), emptyList, emptyList, emptyList, emptyList, List.of(1L, 3L, 4L, 5L, 6L, 7L)),
            Arguments.of(List.of(3L), emptyList, emptyList, emptyList, emptyList, List.of(2L, 8L)),
            Arguments.of(emptyList, List.of(1L), emptyList, emptyList, emptyList,
                List.of(3L, 4L, 5L, 6L)),
            Arguments.of(emptyList, List.of(4L), emptyList, emptyList, emptyList,
                List.of(1L, 2L, 7L, 8L)),
            Arguments.of(emptyList, List.of(1L, 4L), emptyList, emptyList, emptyList, allResourceIds),
            Arguments.of(emptyList, List.of(3L), emptyList, emptyList, emptyList, emptyList),
            Arguments.of(emptyList, emptyList, List.of(1L), emptyList, emptyList, List.of(3L)),
            Arguments.of(emptyList, emptyList, List.of(2L), emptyList, emptyList, List.of(4L, 5L)),
            Arguments.of(emptyList, emptyList, List.of(3L), emptyList, emptyList, List.of(6L)),
            Arguments.of(emptyList, emptyList, List.of(4L), emptyList, emptyList, List.of(1L, 2L, 7L, 8L)),
            Arguments.of(emptyList, emptyList, List.of(1L, 4L), emptyList, emptyList, List.of(1L, 2L, 3L, 7L, 8L)),
            Arguments.of(emptyList, emptyList, List.of(5L), emptyList, emptyList, emptyList),
            Arguments.of(emptyList, emptyList, emptyList, List.of(1L), emptyList, List.of(1L, 3L, 4L, 7L)),
            Arguments.of(emptyList, emptyList, emptyList, List.of(2L), emptyList, List.of(5L)),
            Arguments.of(emptyList, emptyList, emptyList, List.of(3L), emptyList, List.of(6L)),
            Arguments.of(emptyList, emptyList, emptyList, List.of(4L), emptyList, List.of(2L, 8L)),
            Arguments.of(emptyList, emptyList, emptyList, List.of(2L, 4L), emptyList, List.of(2L, 5L, 8L)),
            Arguments.of(emptyList, emptyList, emptyList, List.of(5L), emptyList, emptyList),
            Arguments.of(emptyList, emptyList, emptyList, emptyList, List.of(1L), List.of(1L, 3L, 4L, 5L, 7L)),
            Arguments.of(emptyList, emptyList, emptyList, emptyList, List.of(4L), List.of(2L, 8L)),
            Arguments.of(emptyList, emptyList, emptyList, emptyList, List.of(5L), List.of(6L)),
            Arguments.of(emptyList, emptyList, emptyList, emptyList, List.of(1L, 4L, 5L), allResourceIds),
            Arguments.of(emptyList, emptyList, emptyList, emptyList, List.of(2L), emptyList),
            Arguments.of(List.of(1L), List.of(4L), List.of(4L), List.of(1L), List.of(1L),
                List.of(1L, 7L))
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByNonMVSLOs")
    void findAllBySLOs(List<Long> environmentIds, List<Long> resourceTypeIds,
            List<Long> platformIds, List<Long> regionIds, List<Long> providerIds, List<Long> resultResourceIds,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByNonMVSLOs(sessionManager,
                environmentIds, resourceTypeIds, platformIds, regionIds, providerIds))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Resource::getResourceId).collect(Collectors.toList()))
                    .isEqualTo(resultResourceIds);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByEnsembleId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L),
                List.of(List.of("external-ip", "cluster-url", "pre-pull-timeout"), List.of("pre-pull-timeout")),
                List.of("us-east-1", "fog"), List.of("aws", "custom-fog"), List.of("cloud", "fog"),
                List.of("k8s", "k8s"), List.of("container", "container")),
            Arguments.of(2L, List.of(7L),
                List.of(List.of()), List.of("us-east-1"), List.of("aws"), List.of("cloud"),
                List.of("k8s"), List.of("container")),
            Arguments.of(99L, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByEnsembleId")
    void findAllByEnsembleId(long ensembleId, List<Long> resourceIds, List<List<String>> metrics, List<String> regions,
                List<String> resourceProviders, List<String> environments, List<String> platforms,
                List<String> resourceTypes, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByEnsembleId(sessionManager, ensembleId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Resource::getResourceId).collect(Collectors.toList()))
                    .isEqualTo(resourceIds);
                assertThat(result.stream().map(resource -> resource.getMetricValues()
                    .stream().map(mv -> mv.getMetric().getMetric()).collect(Collectors.toList()))
                    .collect(Collectors.toList()))
                    .isEqualTo(metrics);
                assertThat(result.stream().map(resource -> resource.getMain().getRegion().getName())
                    .collect(Collectors.toList())).isEqualTo(regions);
                assertThat(result.stream().map(resource -> resource.getMain().getRegion().getResourceProvider()
                    .getProvider()).collect(Collectors.toList())).isEqualTo(resourceProviders);
                assertThat(result.stream().map(resource -> resource.getMain().getRegion().getResourceProvider()
                    .getEnvironment().getEnvironment()).collect(Collectors.toList())).isEqualTo(environments);
                assertThat(result.stream().map(resource -> resource.getMain().getPlatform().getPlatform())
                    .collect(Collectors.toList())).isEqualTo(platforms);
                assertThat(result.stream().map(resource -> resource.getMain().getPlatform().getResourceType()
                    .getResourceType()).collect(Collectors.toList())).isEqualTo(resourceTypes);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByDeploymentId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 3L, 5L, 8L), List.of(Set.of("pre-pull-timeout", "external-ip",
                        "cluster-url"), Set.of("deployment-role"), Set.of(), Set.of("pre-pull-timeout")),
                List.of("us-east-1", "us-east-1", "us-west-2", "fog"),
                List.of(ResourceProviderEnum.AWS, ResourceProviderEnum.AWS, ResourceProviderEnum.AWS,
                    ResourceProviderEnum.CUSTOM_FOG),
                List.of("cloud", "cloud", "cloud", "fog"),
                List.of(PlatformEnum.K8S, PlatformEnum.LAMBDA, PlatformEnum.EC2, PlatformEnum.K8S),
                List.of(ResourceTypeEnum.CONTAINER, ResourceTypeEnum.FAAS, ResourceTypeEnum.FAAS,
                    ResourceTypeEnum.CONTAINER),
                List.of(-1, -1, -1, 2L)),
            Arguments.of(99L, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByDeploymentId")
    void findAllByDeploymentId(long deploymentId, List<Long> ids, List<Set<String>> maineResourceMetrics,
            List<String> regions, List<ResourceProviderEnum> resourceProviders, List<String> environments,
            List<PlatformEnum> platforms, List<ResourceTypeEnum> resourceTypes, List<Long> mainResourceIds,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
            .findAllByDeploymentId(sessionManager, deploymentId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(ids.size());
                for(int i = 0; i < result.size(); i++) {
                    Resource resource = result.get(i);
                    assertThat(resource.getResourceId()).isEqualTo(ids.get(i));
                    MainResource mainResource;
                    if (resource instanceof MainResource) {
                        mainResource = (MainResource) resource;
                    } else {
                        mainResource = ((SubResource) resource).getMainResource();
                    }
                    assertThat(mainResource.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toSet()))
                        .isEqualTo(maineResourceMetrics.get(i));
                    assertThat(mainResource.getRegion().getName()).isEqualTo(regions.get(i));
                    assertThat(mainResource.getRegion().getResourceProvider().getProvider())
                        .isEqualTo(resourceProviders.get(i).getValue());
                    assertThat(mainResource.getRegion().getResourceProvider().getEnvironment().getEnvironment())
                        .isEqualTo(environments.get(i));
                    assertThat(mainResource.getPlatform().getPlatform()).isEqualTo(platforms.get(i).getValue());
                    assertThat(mainResource.getPlatform().getResourceType().getResourceType())
                        .isEqualTo(resourceTypes.get(i).getValue());
                    if (resource instanceof SubResource) {
                        assertThat(resource.getMain().getResourceId()).isEqualTo(mainResourceIds.get(i));
                    }
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllLockedByDeploymentId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 3L)),
            Arguments.of(2L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllLockedByDeploymentId")
    void findAllLockedByDeploymentId(long deploymentId, List<Long> resourceIds, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
            .findAllLockedByDeploymentId(sessionManager, deploymentId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Resource::getResourceId).collect(Collectors.toList()))
                    .isEqualTo(resourceIds);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByResourceIdsAndResourceTypes() {
        return Stream.of(
            Arguments.of(Set.of(allResourceIds.toArray()), List.of("faas", "container"), allResourceIds),
            Arguments.of(Set.of(allResourceIds.toArray()), List.of("faas"), List.of(3L, 4L, 5L, 6L)),
            Arguments.of(Set.of(allResourceIds.toArray()), List.of("container"), List.of(1L, 2L, 7L, 8L)),
            Arguments.of(Set.of(1L, 3L), List.of("faas", "container"), List.of(1L, 3L)),
            Arguments.of(Set.of(1L), List.of("faas"), List.of()),
            Arguments.of(Set.of(3L), List.of("container"), List.of()),
            Arguments.of(Set.of(allResourceIds.toArray()), List.of("none"), List.of()),
            Arguments.of(Set.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByResourceIdsAndResourceTypes")
    void findAllByResourceIdsAndResourceTypes(Set<Long> resourceIds, List<String> resourceTypes,
            List<Long> resultResourceIds, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByResourceIdsAndResourceTypes(sessionManager, resourceIds, resourceTypes))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Resource::getResourceId).collect(Collectors.toList()))
                    .isEqualTo(resultResourceIds);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByResourceIdsAndFetch() {
        return Stream.of(
            Arguments.of(allResourceIds, allResourceIds,
                List.of(3, 1, 1, 1, 0, 2, 0, 0), List.of("us-east-1", "fog", "us-east-1", "us-east-1",
                    "us-west-2", "edge", "us-east-1", "fog"),
                List.of("aws", "custom-fog", "aws", "aws", "aws", "custom-edge", "aws", "custom-fog"),
                List.of("cloud", "fog", "cloud", "cloud", "cloud", "edge", "cloud", "fog"), List.of("k8s", "k8s",
                    "lambda", "ec2", "ec2", "openfaas","k8s", "k8s"),
                List.of("container", "container", "faas", "faas", "faas", "faas", "container", "container")),
            Arguments.of(List.of(3L, 99L), List.of(3L), List.of(1), List.of("us-east-1"), List.of("aws"),
                List.of("cloud"), List.of("lambda"), List.of("faas")),
            Arguments.of(List.of(99L), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()),
            Arguments.of(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByResourceIdsAndFetch")
    void findAllByResourceIdsAndFetch(List<Long> resourceIds, List<Long> resultResourceIds, List<Integer> metricsCount,
            List<String> regions, List<String> resourceProviders, List<String> environments, List<String> platforms,
            List<String> resourceTypes, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByResourceIdsAndFetch(sessionManager, resourceIds))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Resource::getResourceId).collect(Collectors.toList()))
                    .isEqualTo(resultResourceIds);
                assertThat(result.stream().map(resource -> resource.getMetricValues().size())
                    .collect(Collectors.toList())).isEqualTo(metricsCount);
                assertThat(result.stream().map(resource -> resource.getMain().getRegion().getName())
                    .collect(Collectors.toList())).isEqualTo(regions);
                assertThat(result.stream().map(resource -> resource.getMain().getRegion().getResourceProvider()
                    .getProvider()).collect(Collectors.toList())).isEqualTo(resourceProviders);
                assertThat(result.stream().map(resource -> resource.getMain().getRegion().getResourceProvider()
                    .getEnvironment().getEnvironment()).collect(Collectors.toList())).isEqualTo(environments);
                assertThat(result.stream().map(resource -> resource.getMain().getPlatform().getPlatform())
                    .collect(Collectors.toList())).isEqualTo(platforms);
                assertThat(result.stream().map(resource -> resource.getMain().getPlatform().getResourceType()
                    .getResourceType()).collect(Collectors.toList())).isEqualTo(resourceTypes);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllSubresources() {
        return Stream.of(
            Arguments.of(1L, 7L, List.of()),
            Arguments.of(2L, 8L, List.of()),
            Arguments.of(3L, -1L, List.of()),
            Arguments.of(7L, -1L, List.of()),
            Arguments.of(99L, -1L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllSubresources")
    void findAllSubresources(Long resourceId, Long resultResourceId, List<String> metrics,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllSubresources(sessionManager, resourceId))
            .subscribe(result -> testContext.verify(() -> {
                if (resultResourceId == -1L) {
                    assertThat(result.size()).isEqualTo(0);
                } else {
                    assertThat(result.get(0).getResourceId()).isEqualTo(resultResourceId);
                    assertThat(result.get(0).getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toList())).isEqualTo(metrics);
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindClusterByName() {
        return Stream.of(
            Arguments.of("r1", true, 1L, List.of("external-ip", "cluster-url", "pre-pull-timeout"), 7L,
                List.of()),
            Arguments.of("r2", true, 2L, List.of("pre-pull-timeout"), 8L, List.of()),
            Arguments.of("r3", false, -1L, List.of(), -1L, List.of()),
            Arguments.of("sr1", false, -1L, List.of(), -1L, List.of()),
            Arguments.of("sr12", false, -1L, List.of(), -1L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindClusterByName")
    void findClusterByName(String name, boolean exists, long resourceId, List<String> clusterMetrics,
            long subResourceId, List<String> nodeMetrics, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findClusterByName(sessionManager, name))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getResourceId()).isEqualTo(resourceId);
                    assertThat(result.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toList())).isEqualTo(clusterMetrics);
                    assertThat(result.getMain().getSubResources().size()).isEqualTo(1);
                    SubResource sr = result.getMain().getSubResources().get(0);
                    assertThat(sr.getResourceId()).isEqualTo(subResourceId);
                    assertThat(sr.getMetricValues().stream().map(mv -> mv.getMetric().getMetric())
                        .collect(Collectors.toList())).isEqualTo(nodeMetrics);
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
    void findAllFunctionDeployments(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllFunctionDeploymentTargets)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(1);
                List<FindAllFunctionDeploymentScrapeTargetsDTO> resultList = new ArrayList<>();
                CollectionUtils.addAll(resultList, result);
                assertThat(resultList.get(0).getDeploymentId()).isEqualTo(1L);
                assertThat(resultList.get(0).getResourceDeploymentId()).isEqualTo(3L);
                assertThat(resultList.get(0).getResourceId()).isEqualTo(5L);
                assertThat(resultList.get(0).getBaseUrl()).isEqualTo("https://aws.com");
                assertThat(resultList.get(0).getMetricsPort()).isEqualTo(9100);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllOpenFaaSTargets(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllOpenFaaSTargets)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(1);
                List<FindAllOpenFaaSScrapeTargetsDTO> resultList = new ArrayList<>();
                CollectionUtils.addAll(resultList, result);
                assertThat(resultList.get(0).getResourceId()).isEqualTo(6L);
                assertThat(resultList.get(0).getBaseUrl()).isEqualTo("http://localhost");
                assertThat(resultList.get(0).getMetricsPort().compareTo(BigDecimal.valueOf(9100))).isEqualTo(0);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
