package at.uibk.dps.rm.service.database.resourceprovider;


import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link VPCServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class VPCServiceImplTest {

    private VPCService vpcService;

    @Mock
    private VPCRepository vpcRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        vpcService = new VPCServiceImpl(vpcRepository, regionRepository, sessionFactory);
    }



    @Test
    void findOne(VertxTestContext testContext) {
        long vpcId = 1L, regionId = 1L, accountId = 2L;
        Region region = TestResourceProviderProvider.createRegion(regionId, "aws");
        Account account = TestAccountProvider.createAccount(accountId);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region, account);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findByIdAndFetch(session, vpcId)).thenReturn(CompletionStages.completedFuture(vpc));

        vpcService.findOne(vpcId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneNotExists(VertxTestContext testContext) {
        long vpcId = 1L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findByIdAndFetch(session, vpcId)).thenReturn(CompletionStages.completedFuture(null));

        vpcService.findOne(vpcId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        long accountId = 1L;
        Region r1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        Region r2 = TestResourceProviderProvider.createRegion(1L, "us-west");
        VPC vpc1 = TestResourceProviderProvider.createVPC(1L, r1);
        VPC vpc2 = TestResourceProviderProvider.createVPC(2L, r2);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findAllByAccountIdAndFetch(session, accountId))
            .thenReturn(CompletionStages.completedFuture(List.of(vpc1, vpc2)));

        vpcService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("vpc_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(0).getJsonObject("region")).isNotNull();
                assertThat(result.getJsonObject(1).getJsonObject("region")).isNotNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByRegionIdAndAccountIdExists(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;
        Region region = TestResourceProviderProvider.createRegion(regionId, "aws");
        Account account = TestAccountProvider.createAccount(accountId);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region, account);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId))
            .thenReturn(CompletionStages.completedFuture(vpc));

        vpcService.findOneByRegionIdAndAccountId(regionId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByRegionIdAndAccountIdNotExists(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId))
            .thenReturn(CompletionStages.completedFuture(null));

        vpcService.findOneByRegionIdAndAccountId(regionId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void existsOneByRegionIdAndAccountId(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;
        Region region = TestResourceProviderProvider.createRegion(regionId, "aws");
        Account account = TestAccountProvider.createAccount(accountId);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region, account);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId))
            .thenReturn(CompletionStages.completedFuture(vpc));

        vpcService.existsOneByRegionIdAndAccountId(regionId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void existsOneByRegionIdAndAccountIdNotExists(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId))
            .thenReturn(CompletionStages.completedFuture(null));

        vpcService.existsOneByRegionIdAndAccountId(regionId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}
