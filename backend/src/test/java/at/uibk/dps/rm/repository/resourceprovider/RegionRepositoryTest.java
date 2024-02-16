package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
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
 * Implements tests for the {@link RegionRepository} class.
 *
 * @author matthi-g
 */
public class RegionRepositoryTest extends DatabaseTest {

    private final RegionRepository repository = new RegionRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionCompletable(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-2");
            Platform p1 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg2);
            SubResource sr1 = TestResourceProvider.createSubResourceWithoutMVs(null, "r3", (MainResource) r1);
            SubResource sr2 = TestResourceProvider.createSubResourceWithoutMVs(null, "r4", (MainResource) r2);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            Metric mClusterUrl = TestMetricProvider.createMetric(12L);
            Metric mPrePullTimeout = TestMetricProvider.createMetric(17L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mClusterUrl, r1, "localhost");
            MetricValue mv3 = TestMetricProvider.createMetricValue(null, mPrePullTimeout, r1, 2.0);
            MetricValue mv5 = TestMetricProvider.createMetricValue(null, mAvailability, r2, 0.99);
            return sessionManager.persist(r1)
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(sr1))
                .flatMap(res -> sessionManager.persist(sr2))
                .flatMapCompletable(res -> sessionManager.persist(new MetricValue[]{mv1, mv2, mv3, mv5}));
        }).blockingSubscribe(() -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, true, us-east-1, aws, cloud",
        "2, true, us-west-2, aws, cloud",
        "3, true, edge, custom-edge, edge",
        "4, true, private-cloud, custom-cloud, cloud",
        "5, false, none, none, none",
    })
    void findByIdAndFetch(long id, boolean exists, String name, String resourceProvider, String environment,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, id))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getRegionId()).isEqualTo(id);
                    assertThat(result.getName()).isEqualTo(name);
                    assertThat(result.getResourceProvider().getProvider()).isEqualTo(resourceProvider);
                    assertThat(result.getResourceProvider().getEnvironment().getEnvironment()).isEqualTo(environment);
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

    @ParameterizedTest
    @CsvSource({
        "us-east-1, 1, true, 1, aws, cloud",
        "us-east-1, 2, false, -1, none, none",
        "us-west-2, 1, true, 2, aws, cloud",
        "us-west-2, 2, false, -1, none, none",
        "edge, 5, true, 3, custom-edge, edge",
        "edge, 1, false, -1, none, none",
        "private-cloud, 4, true, 4, custom-cloud, cloud",
        "private-cloud, 2, false, -1, none, none",
        "eu-west-1, 4, false, -1, none, none",
    })
    void findByIdAndFetch(String name, long providerId, boolean exists, long id, String resourceProvider,
            String environment, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findOneByNameAndProviderId(sessionManager, name, providerId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getRegionId()).isEqualTo(id);
                    assertThat(result.getName()).isEqualTo(name);
                    assertThat(result.getResourceProvider().getProvider()).isEqualTo(resourceProvider);
                    assertThat(result.getResourceProvider().getEnvironment().getEnvironment()).isEqualTo(environment);
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
                assertThat(result.size()).isEqualTo(4);
                assertThat(result.get(0).getRegionId()).isEqualTo(1L);
                assertThat(result.get(0).getName()).isEqualTo("us-east-1");
                assertThat(result.get(0).getResourceProvider().getProvider()).isEqualTo("aws");
                assertThat(result.get(0).getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(1).getRegionId()).isEqualTo(2L);
                assertThat(result.get(1).getName()).isEqualTo("us-west-2");
                assertThat(result.get(1).getResourceProvider().getProvider()).isEqualTo("aws");
                assertThat(result.get(1).getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(2).getRegionId()).isEqualTo(4L);
                assertThat(result.get(2).getName()).isEqualTo("private-cloud");
                assertThat(result.get(2).getResourceProvider().getProvider()).isEqualTo("custom-cloud");
                assertThat(result.get(2).getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("cloud");
                assertThat(result.get(3).getRegionId()).isEqualTo(3L);
                assertThat(result.get(3).getName()).isEqualTo("edge");
                assertThat(result.get(3).getResourceProvider().getProvider()).isEqualTo("custom-edge");
                assertThat(result.get(3).getResourceProvider().getEnvironment().getEnvironment())
                    .isEqualTo("edge");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByProvider() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L)),
            Arguments.of(4L, List.of(4L)),
            Arguments.of(5L, List.of(3L)),
            Arguments.of(99L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByProvider")
    void findAllByProviderId(long providerId, List<Long> expected, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByProviderId(sessionManager, providerId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Region::getRegionId).collect(Collectors.toList())).isEqualTo(expected);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    private static Stream<Arguments> provideFindAllByPlatformId() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L), List.of("aws", "aws")),
            Arguments.of(2L, List.of(1L, 2L), List.of("aws", "aws")),
            Arguments.of(3L, List.of(1L, 2L, 3L, 4L), List.of("aws", "aws", "custom-edge", "custom-cloud")),
            Arguments.of(4L, List.of(1L, 2L, 3L, 4L), List.of("aws", "aws", "custom-edge", "custom-cloud")),
            Arguments.of(99L, List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByPlatformId")
    void findAllByPlatformId(long platformId, List<Long> expected, List<String> providers,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByPlatformId(sessionManager, platformId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Region::getRegionId).collect(Collectors.toList())).isEqualTo(expected);
                assertThat(result.stream().map(region -> region.getResourceProvider().getProvider())
                    .collect(Collectors.toList())).isEqualTo(providers);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true",
        "1, 2, true",
        "1, 3, true",
        "1, 4, true",
        "1, 5, false",
        "2, 1, true",
        "2, 2, true",
        "2, 3, true",
        "2, 4, true",
        "2, 5, false",
        "3, 1, false",
        "3, 2, false",
        "3, 3, true",
        "3, 4, true",
        "3, 5, false",
        "4, 1, false",
        "4, 2, false",
        "4, 3, true",
        "4, 4, true",
        "4, 5, false",
    })
    void findByRegionIdAndPlatformId(long regionId, long platformId, boolean exists, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByRegionIdAndPlatformId(sessionManager, regionId, platformId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getRegionId()).isEqualTo(regionId);
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
