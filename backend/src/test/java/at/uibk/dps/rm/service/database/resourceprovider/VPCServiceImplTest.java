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
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
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
    
    private final SessionManager sessionManager = new SessionManager(session);

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

        SessionMockHelper.mockTransaction(sessionFactory, sessionManager);
        when(vpcRepository.findByIdAndFetch(sessionManager, vpcId)).thenReturn(Maybe.just(vpc));

        vpcService.findOne(vpcId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneNotExists(VertxTestContext testContext) {
        long vpcId = 1L;

        SessionMockHelper.mockTransaction(sessionFactory, sessionManager);
        when(vpcRepository.findByIdAndFetch(sessionManager, vpcId)).thenReturn(Maybe.empty());

        vpcService.findOne(vpcId, testContext.succeeding(result -> testContext.verify(() -> {
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

        SessionMockHelper.mockTransaction(sessionFactory, sessionManager);
        when(vpcRepository.findAllByAccountIdAndFetch(sessionManager, accountId))
            .thenReturn(Single.just(List.of(vpc1, vpc2)));

        vpcService.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("vpc_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(0).getJsonObject("region")).isNotNull();
                assertThat(result.getJsonObject(1).getJsonObject("region")).isNotNull();
                testContext.completeNow();
            })));
    }
}
