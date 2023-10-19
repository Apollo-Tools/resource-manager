package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

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

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionCompletable(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(3L, "edge");
            Platform p1 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Platform p2 = TestPlatformProvider.createPlatformContainer(1L, "lambda");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg2);
            Resource r3 = TestResourceProvider.createResource(null, "r3", p2, reg1);
            SubResource sr1 = TestResourceProvider.createSubResourceWithoutMVs(null, "r4", (MainResource) r1);
            SubResource sr2 = TestResourceProvider.createSubResourceWithoutMVs(null, "r5", (MainResource) r2);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            Metric mClusterUrl = TestMetricProvider.createMetric(12L);
            Metric mPrePullTimeout = TestMetricProvider.createMetric(17L);
            Metric mHostname = TestMetricProvider.createMetric(21L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mClusterUrl, r1, "localhost");
            MetricValue mv3 = TestMetricProvider.createMetricValue(null, mPrePullTimeout, r1, 2.0);
            MetricValue mv5 = TestMetricProvider.createMetricValue(null, mAvailability, r2, 0.99);
            MetricValue mv6 = TestMetricProvider.createMetricValue(null, mHostname, sr1, "node1");
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1");
            Ensemble e2 = TestEnsembleProvider.createEnsemble(null, 2L, "e2");
            ResourceEnsemble re1 = TestEnsembleProvider.createResourceEnsemble(null, e1, r1);
            ResourceEnsemble re2 = TestEnsembleProvider.createResourceEnsemble(null, e1, r2);
            ResourceEnsemble re3 = TestEnsembleProvider.createResourceEnsemble(null, e2, sr1);

            return sessionManager.persist(r1)
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(r3))
                .flatMap(res -> sessionManager.persist(sr1))
                .flatMap(res -> sessionManager.persist(sr2))
                .flatMap(res -> sessionManager.persist(e1))
                .flatMap(res -> sessionManager.persist(e2))
                .flatMap(res -> sessionManager.persist(re1))
                .flatMap(res -> sessionManager.persist(re2))
                .flatMap(res -> sessionManager.persist(re3))
                .flatMapCompletable(res -> sessionManager.persist(new MetricValue[]{mv1, mv2, mv3, mv5, mv6}));
        }).blockingSubscribe(() -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "r1, true, 1",
        "r2, true, 2",
        "r3, true, 3",
        "r4, false, -1",
        "r5, false, -1",
        "r6, false, -1"
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
        List<String> r1Metrics = List.of("availability", "cluster-url", "pre-pull-timeout");
        List<String> r2Metrics = List.of("availability");
        List<String> r3Metrics = List.of("hostname");
        String rpAws = "aws";
        String rpCustomEdge = "custom-edge";
        String eEdge = "edge";
        String eCloud = "cloud";
        return Stream.of(
            Arguments.of(1L, true, r1Metrics, r1Metrics, "us-east-1", rpAws, eCloud),
            Arguments.of(2L, true, r2Metrics, r2Metrics, "edge", rpCustomEdge, eEdge),
            Arguments.of(3L, true, List.of(),  List.of(), "us-east-1", rpAws, eCloud),
            Arguments.of(4L, true, r3Metrics, r1Metrics, "us-east-1", rpAws, eCloud),
            Arguments.of(5L, true, List.of(),  r2Metrics, "edge", rpCustomEdge, eEdge),
            Arguments.of(99L, false, List.of(), List.of(), "", "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindByIdAndFetch")
    void findByIdAndFetch(long resourceId, boolean exists, List<String> metrics, List<String> mainMetrics,
            String region, String resourceProvider, String environment, VertxTestContext testContext) {
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
                    assertThat(result.getMain().getPlatform().getPlatform())
                        .isEqualTo(resourceId==3L ? "lambda" : "k8s");
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
    void findAllAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.get(0).getResourceId()).isEqualTo(1L);
                assertThat(result.get(0).getMain().getRegion().getName()).isEqualTo("us-east-1");
                assertThat(result.get(0).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("aws");
                assertThat(result.get(0).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(0).getMain().getPlatform().getPlatform()).isEqualTo("k8s");
                assertThat(result.get(0).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("container");
                assertThat(result.get(0).getMain().getSubResources().size()).isEqualTo(1);
                assertThat(result.get(0).getMain().getSubResources().get(0).getResourceId()).isEqualTo(4L);
                assertThat(result.get(1).getResourceId()).isEqualTo(2L);
                assertThat(result.get(1).getMain().getRegion().getName()).isEqualTo("edge");
                assertThat(result.get(1).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("custom-edge");
                assertThat(result.get(1).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("edge");
                assertThat(result.get(1).getMain().getPlatform().getPlatform()).isEqualTo("k8s");
                assertThat(result.get(1).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("container");
                assertThat(result.get(1).getMain().getSubResources().size()).isEqualTo(1);
                assertThat(result.get(1).getMain().getSubResources().get(0).getResourceId()).isEqualTo(5L);
                assertThat(result.get(2).getResourceId()).isEqualTo(3L);
                assertThat(result.get(2).getMain().getRegion().getName()).isEqualTo("us-east-1");
                assertThat(result.get(2).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("aws");
                assertThat(result.get(2).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(2).getMain().getPlatform().getPlatform()).isEqualTo("lambda");
                assertThat(result.get(2).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("faas");
                assertThat(result.get(2).getMain().getSubResources().size()).isEqualTo(0);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllBySLOsNoSLOs(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllBySLOs(sessionManager, List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of()))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(5);
                assertThat(result.get(0).getResourceId()).isEqualTo(1L);
                assertThat(result.get(0).getMain().getRegion().getName()).isEqualTo("us-east-1");
                assertThat(result.get(0).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("aws");
                assertThat(result.get(0).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(0).getMain().getPlatform().getPlatform()).isEqualTo("k8s");
                assertThat(result.get(0).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("container");
                assertThat(result.get(1).getResourceId()).isEqualTo(2L);
                assertThat(result.get(1).getMain().getRegion().getName()).isEqualTo("edge");
                assertThat(result.get(1).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("custom-edge");
                assertThat(result.get(1).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("edge");
                assertThat(result.get(1).getMain().getPlatform().getPlatform()).isEqualTo("k8s");
                assertThat(result.get(1).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("container");
                assertThat(result.get(2).getResourceId()).isEqualTo(3L);
                assertThat(result.get(2).getMain().getRegion().getName()).isEqualTo("us-east-1");
                assertThat(result.get(2).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("aws");
                assertThat(result.get(2).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(2).getMain().getPlatform().getPlatform()).isEqualTo("lambda");
                assertThat(result.get(2).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("faas");
                assertThat(result.get(3).getResourceId()).isEqualTo(4L);
                assertThat(result.get(3).getMain().getRegion().getName()).isEqualTo("us-east-1");
                assertThat(result.get(3).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("aws");
                assertThat(result.get(3).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(3).getMain().getPlatform().getPlatform()).isEqualTo("k8s");
                assertThat(result.get(3).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("container");
                assertThat(result.get(4).getResourceId()).isEqualTo(5L);
                assertThat(result.get(4).getMain().getRegion().getName()).isEqualTo("edge");
                assertThat(result.get(4).getMain().getRegion().getResourceProvider().getProvider())
                    .isEqualTo("custom-edge");
                assertThat(result.get(4).getMain().getRegion().getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("edge");
                assertThat(result.get(4).getMain().getPlatform().getPlatform()).isEqualTo("k8s");
                assertThat(result.get(4).getMain().getPlatform().getResourceType().getResourceType())
                    .isEqualTo("container");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllBySLOs() {
        List<Long> emptyList = List.of();
        List<Long> allRs = List.of(1L, 2L, 3L, 4L, 5L);
        return Stream.of(
            Arguments.of(List.of("availability"), emptyList, emptyList, emptyList, emptyList, emptyList,
                List.of(1L, 2L)),
            Arguments.of(List.of("hostname"), emptyList, emptyList, emptyList, emptyList, emptyList,
                List.of(4L)),
            Arguments.of(List.of("availability, latency"), emptyList, emptyList, emptyList, emptyList, emptyList,
                emptyList),
            Arguments.of(List.of(), List.of(1L), emptyList, emptyList, emptyList, emptyList, List.of(1L, 3L, 4L)),
            Arguments.of(List.of(), List.of(1L, 2L), emptyList, emptyList, emptyList, emptyList, allRs),
            Arguments.of(List.of(), List.of(3L), emptyList, emptyList, emptyList, emptyList, emptyList),
            Arguments.of(List.of(), emptyList, List.of(4L), emptyList, emptyList, emptyList,
                List.of(1L, 2L, 4L, 5L)),
            Arguments.of(List.of(), emptyList, List.of(1L, 4L), emptyList, emptyList, emptyList, allRs),
            Arguments.of(List.of(), emptyList, List.of(2L), emptyList, emptyList, emptyList, emptyList),
            Arguments.of(List.of(), emptyList, emptyList, List.of(1L), emptyList, emptyList, List.of(3L)),
            Arguments.of(List.of(), emptyList, emptyList, List.of(1L, 4L), emptyList, emptyList, allRs),
            Arguments.of(List.of(), emptyList, emptyList, List.of(2L), emptyList, emptyList, emptyList),
            Arguments.of(List.of(), emptyList, emptyList, emptyList, List.of(1L), emptyList, List.of(1L, 3L, 4L)),
            Arguments.of(List.of(), emptyList, emptyList, emptyList, List.of(1L, 3L), emptyList, allRs),
            Arguments.of(List.of(), emptyList, emptyList, emptyList, List.of(2L), emptyList, emptyList),
            Arguments.of(List.of(), emptyList, emptyList, emptyList, emptyList, List.of(1L), List.of(1L, 3L, 4L)),
            Arguments.of(List.of(), emptyList, emptyList, emptyList, emptyList, List.of(1L, 5L), allRs),
            Arguments.of(List.of(), emptyList, emptyList, emptyList, List.of(2L), emptyList, emptyList),
            Arguments.of(List.of("availability"), List.of(1L), List.of(4L), List.of(4L), List.of(1L),
                List.of(1L), List.of(1L))
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllBySLOs")
    void findAllBySLOs(List<String> metrics, List<Long> environmentIds, List<Long> resourceTypeIds,
            List<Long> platformIds, List<Long> regionIds, List<Long> providerIds, List<Long> resultResourceIds,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllBySLOs(sessionManager, metrics,
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
                List.of(List.of("availability", "cluster-url", "pre-pull-timeout"), List.of("availability")),
                List.of("us-east-1", "edge"), List.of("aws", "custom-edge"), List.of("cloud", "edge"),
                List.of("k8s", "k8s"), List.of("container", "container")),
            Arguments.of(2L, List.of(4L),
                List.of(List.of("hostname")), List.of("us-east-1"), List.of("aws"), List.of("cloud"),
                List.of("k8s"), List.of("container"))
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

    private static Stream<Arguments> provideFindAllByResourceIdsAndResourceTypes() {
        return Stream.of(
            Arguments.of(Set.of(1L, 2L, 3L, 4L, 5L), List.of("faas", "container"), List.of(1L, 2L, 3L, 4L, 5L)),
            Arguments.of(Set.of(1L, 2L, 3L, 4L, 5L), List.of("faas"), List.of(3L)),
            Arguments.of(Set.of(1L, 2L, 3L, 4L, 5L), List.of("container"), List.of(1L, 2L, 4L, 5L)),
            Arguments.of(Set.of(1L, 3L), List.of("faas", "container"), List.of(1L, 3L)),
            Arguments.of(Set.of(1L), List.of("faas"), List.of()),
            Arguments.of(Set.of(3L), List.of("container"), List.of()),
            Arguments.of(Set.of(1L, 2L, 3L, 4L, 5L), List.of("none"), List.of()),
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
            Arguments.of(List.of(1L, 2L, 3L, 4L, 5L), List.of(1L, 2L, 3L, 4L, 5L),
                List.of(3, 1, 0, 1, 0), List.of("us-east-1", "edge", "us-east-1", "us-east-1", "edge"),
                List.of("aws", "custom-edge", "aws", "aws", "custom-edge"),
                List.of("cloud", "edge", "cloud", "cloud", "edge"), List.of("k8s", "k8s", "lambda", "k8s", "k8s"),
                List.of("container", "container", "faas", "container", "container")),
            Arguments.of(List.of(3L, 6L), List.of(3L), List.of(0), List.of("us-east-1"), List.of("aws"),
                List.of("cloud"), List.of("lambda"), List.of("faas")),
            Arguments.of(List.of(6L), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()),
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
            Arguments.of(1L, 4L, List.of("hostname")),
            Arguments.of(2L, 5L, List.of()),
            Arguments.of(3L, -1L, List.of()),
            Arguments.of(7L, -1L, List.of())
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
            Arguments.of("r1", true, 1L, List.of("availability", "cluster-url", "pre-pull-timeout"), 4L,
                List.of("hostname")),
            Arguments.of("r2", true, 2L, List.of("availability"), 5L, List.of()),
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
}
