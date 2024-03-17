package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
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
 * Implements tests for the {@link VPCRepository} class.
 *
 * @author matthi-g
 */
public class VPCRepositoryTest extends DatabaseTest {

    private final VPCRepository repository = new VPCRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-2");
            VPC vpc1 = TestResourceProviderProvider.createVPC(null, reg1, accountAdmin);
            VPC vpc2 = TestResourceProviderProvider.createVPC(null, reg2, accountAdmin);
            VPC vpc3 = TestResourceProviderProvider.createVPC(null, reg2, accountDefault);
            return sessionManager.persist(vpc1)
                .flatMap(res -> sessionManager.persist(vpc2))
                .flatMap(res -> sessionManager.persist(vpc3));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, true, us-east-1, aws",
        "2, true, us-west-2, aws",
        "3, true, us-west-2, aws",
    })
    void findByIdAndFetch(long vpcId, boolean exists, String region, String resourceProvider,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, vpcId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getVpcId()).isEqualTo(vpcId);
                    assertThat(result.getRegion().getName()).isEqualTo(region);
                    assertThat(result.getRegion().getResourceProvider().getProvider()).isEqualTo(resourceProvider);
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
        "1, 1, true, 1, us-east-1, aws",
        "1, 2, false, -1, none, none",
        "1, 3, false, -1, none, none",
        "2, 1, true, 2, us-west-2, aws",
        "2, 2, true, 3, us-west-2, aws",
    })
    void findByIdAndFetch(long regionId, long accountId, boolean exists, long vpcId, String region,
            String resourceProvider, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByRegionIdAndAccountId(sessionManager, regionId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getVpcId()).isEqualTo(vpcId);
                    assertThat(result.getRegion().getName()).isEqualTo(region);
                    assertThat(result.getRegion().getResourceProvider().getProvider()).isEqualTo(resourceProvider);
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
        "1, 1, true",
        "1, 2, false",
        "1, 3, false",
        "2, 1, true",
        "2, 2, false",
        "3, 1, false",
        "3, 2, true",
    })
    void findByIdAndAccountId(long vpcId, long accountId, boolean exists, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, vpcId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getVpcId()).isEqualTo(vpcId);
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
        "1, 1, us-east-1, aws, true",
        "1, 2, x, x, false",
        "1, 3, x, x, false",
        "2, 1, us-west-2, aws, true",
        "2, 2, x, x, false",
        "3, 1, x, x, false",
        "3, 2, us-west-2, aws, true",
    })
    void findByIdAndAccountIdAndFetch(long vpcId, long accountId, String region, String provider, boolean exists,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountIdAndFetch(sessionManager, vpcId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getVpcId()).isEqualTo(vpcId);
                    assertThat(result.getRegion().getName()).isEqualTo(region);
                    assertThat(result.getRegion().getResourceProvider().getProvider()).isEqualTo(provider);
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

    private static Stream<Arguments> provideFindAllByAccount() {
        return Stream.of(
            Arguments.of(1L, List.of(1L, 2L), List.of("us-east-1", "us-west-2"), List.of("aws", "aws")),
            Arguments.of(2L, List.of(3L), List.of("us-west-2"), List.of("aws")),
            Arguments.of(99L, List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByAccount")
    void findAllByPlatformId(long accountId, List<Long> vpcIds, List<String> regions, List<String> providers,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByAccountIdAndFetch(sessionManager, accountId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(VPC::getVpcId).collect(Collectors.toList())).isEqualTo(vpcIds);
                assertThat(result.stream().map(vpc -> vpc.getRegion().getName()).collect(Collectors.toList()))
                    .isEqualTo(regions);
                assertThat(result.stream().map(vpc -> vpc.getRegion().getResourceProvider().getProvider())
                    .collect(Collectors.toList())).isEqualTo(providers);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
